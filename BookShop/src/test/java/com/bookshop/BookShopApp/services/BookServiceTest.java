package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.book.links.Book2UserType;
import com.bookshop.BookShopApp.structure.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class BookServiceTest {

    private final BookService bookService;
    private final UserService userService;
    private final UserRegister userRegister;
    private RegistrationForm registrationForm;

    @Autowired
    public BookServiceTest(BookService bookService, UserService userService, UserRegister userRegister) {
        this.bookService = bookService;
        this.userService = userService;
        this.userRegister = userRegister;
    }

    @BeforeEach
    void setUp() {
        registrationForm = new RegistrationForm();
        registrationForm.setEmail("test1@mail.org");
        registrationForm.setName("TesterBookService");
        registrationForm.setPhone("9031232333");
    }

    @AfterEach
    void tearDown() {
        registrationForm = null;
    }

    @Test
    void getPageOfRecommendedBooks() {
        Pageable nextPage = PageRequest.of(0,20);
        Page<Book> result = bookService.getPageOfRecommendedBooks(0, 20);
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
    }

    @Test
    void updateBookPopularity() {
        Book book = bookService.addBook((short) 0, "", "","", "", 0, (short) 0);
        User user1 = userRegister.registerNewUser(registrationForm, null);
        User user2 = userRegister.registerNewUser(registrationForm, null);
        User user3 = userRegister.registerNewUser(registrationForm, null);
        assertTrue(book.getPopularity() == 0);
        bookService.addBook2User(book.getId(), user1.getId(), 1);
        bookService.addBook2User(book.getId(), user2.getId(), 2);
        bookService.addBook2User(book.getId(), user3.getId(), 3);
        bookService.updateBookPopularity(book.getId());
        Book updatedBook = bookService.getBookById(book.getId());
        assertTrue(updatedBook.getPopularity() == 2.1);
        bookService.removeBook2UserByUserId(user1.getId());
        bookService.removeBook2UserByUserId(user2.getId());
        bookService.removeBook2UserByUserId(user3.getId());
        bookService.removeBookById(book.getId());
        userService.removeUserById(user1.getId());
        userService.removeUserById(user2.getId());
        userService.removeUserById(user3.getId());
    }


}