package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.services.Book2UserService;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.services.UserRegister;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.book.links.Book2User;
import com.bookshop.BookShopApp.structure.book.review.BookReview;
import com.bookshop.BookShopApp.structure.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class BooksRestApiControllerTests {

    private final MockMvc mockMvc;
    private final BookService bookService;
    private final UserService userService;
    private final UserRegister userRegister;
    private final Book2UserService book2UserService;

    @Autowired
    public BooksRestApiControllerTests(MockMvc mockMvc, BookService bookService, UserService userService, UserRegister userRegister, Book2UserService book2UserService) {
        this.mockMvc = mockMvc;
        this.bookService = bookService;
        this.userService = userService;
        this.userRegister = userRegister;
        this.book2UserService = book2UserService;
    }


    @Test
    void handleChangeBookStatus() throws Exception {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setEmail("testChangeStatus@mail.org");
        registrationForm.setName("TesterChangeStatus");
        registrationForm.setPhone("+7(903) 123-55-55");
        Book book1 = bookService.addBook((short) 0, "", "","", "", 0, (short) 0);
        Book book2 = bookService.addBook((short) 0, "", "","", "", 0, (short) 0);
        User user = userRegister.registerNewUser(registrationForm, null);
        Cookie cookie = new Cookie("bookShop", String.valueOf(user.getId()));


        testChangeBookStatus(String.valueOf(book1.getId()), "CART", true, cookie);
        Book2User book2User = book2UserService.getBook2UserByUserIdBookIdAndStatus(user.getId(), book1.getId(), "CART");
        assertNotNull(book2User);
        testChangeBookStatus(String.valueOf(book1.getId()), "UNLINK", true, cookie);
        book2User = book2UserService.getBook2UserByUserIdBookIdAndStatus(user.getId(), book1.getId(), "CART");
        assertNull(book2User);
        testChangeBookStatus(String.valueOf(book1.getId()), "UNLINK", false, cookie);

        testChangeBookStatus(String.valueOf(book1.getId()).concat(",").concat(String.valueOf(book2.getId())), "CART", true, cookie);
        book2User = book2UserService.getBook2UserByUserIdBookIdAndStatus(user.getId(), book1.getId(), "CART");
        Book2User book2User2 = book2UserService.getBook2UserByUserIdBookIdAndStatus(user.getId(), book2.getId(), "CART");
        assertNotNull(book2User);
        assertNotNull(book2User2);
        testChangeBookStatus(String.valueOf(book1.getId()).concat(",").concat(String.valueOf(book2.getId())), "UNLINK", true, cookie);
        book2User = book2UserService.getBook2UserByUserIdBookIdAndStatus(user.getId(), book1.getId(), "CART");
        book2User2 = book2UserService.getBook2UserByUserIdBookIdAndStatus(user.getId(), book2.getId(), "CART");
        assertNull(book2User);
        assertNull(book2User2);

        bookService.removeBookById(book1.getId());
        bookService.removeBookById(book2.getId());
        userService.removeUserById(user.getId());
    }

    private void testChangeBookStatus(String booksIds, String status, boolean result, Cookie cookie) throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/changeBookStatus")
                .param("booksIds[]", booksIds)
                .param("status", status)
                .cookie(cookie);

        mockMvc.perform(mockRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value(result));

    }

    @Test
    void handleAddBookReviewLike() throws Exception {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setEmail("testReviewRating@mail.org");
        registrationForm.setName("TesterReviewRating");
        registrationForm.setPhone("+7(903) 123-66-66");
        Book book = bookService.addBook((short) 0, "", "","", "", 0, (short) 0);
        User user = userRegister.registerNewUser(registrationForm, null);
        Cookie cookie = new Cookie("bookShop", String.valueOf(user.getId()));
        BookReview bookReview = new BookReview();
        bookReview.setBookId(book.getId());
        bookReview.setUserId(user.getId());
        bookReview.setText("Lorem ipsum");
        bookReview.setTime(LocalDateTime.now());
        Integer reviewId = bookService.newBookReview(bookReview);

        testAddBookReviewLike(reviewId, cookie, true);
        BookReview newBookReview = bookService.getBookReviewById(reviewId);
        assertEquals(1, newBookReview.getRating());
        testAddBookReviewLike(reviewId, cookie, false);
        newBookReview = bookService.getBookReviewById(reviewId);
        assertEquals(1, newBookReview.getRating());

        bookService.removeBookReviewLikeByUserIdAndReviewId(user.getId(), reviewId);
        bookService.removeBookReviewById(reviewId);
        bookService.removeBookById(book.getId());
        userService.removeUserById(user.getId());
    }

    private void testAddBookReviewLike(Integer reviewId, Cookie cookie, boolean result) throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/rateBookReview")
                .param("reviewId", String.valueOf(reviewId))
                .param("value", "1")
                .cookie(cookie);

        mockMvc.perform(mockRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value(result));
    }
}