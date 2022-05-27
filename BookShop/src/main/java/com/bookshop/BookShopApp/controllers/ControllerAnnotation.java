package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.services.UserRegister;
import com.bookshop.BookShopApp.data.SearchWordDto;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.book.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.Cookie;
import java.util.List;

@ControllerAdvice(annotations = Controller.class)
public class ControllerAnnotation {

    private final BookService bookService;
    private final UserService userService;
    private final UserRegister userRegister;

    @Autowired
    public ControllerAnnotation(BookService bookService, UserService userService, UserRegister userRegister) {
        this.bookService = bookService;
        this.userService = userService;
        this.userRegister = userRegister;
    }

    @ModelAttribute("searchWordDto")
    public SearchWordDto searchWordDto() {
        return new SearchWordDto();
    }

    @ModelAttribute(name = "cartCount")
    public Integer bookCartCount(@CookieValue(value = "bookShop", required = false) String bookShop)  {
        Integer userId = userService.getCurrentUserId(bookShop);
        List<Book> currentUserBooks = bookService.getBooksByUserIdAndStatus(userId, "CART");
        return Math.toIntExact(currentUserBooks.size());
    }

    @ModelAttribute(name = "postponedCount")
    public Integer bookPostponedCount(@CookieValue(value = "bookShop", required = false) String bookShop)  {
        Integer userId = userService.getCurrentUserId(bookShop);
        List<Book> booksFromCookieIds = bookService.getBooksByUserIdAndStatus(userId, "KEPT");
        return Math.toIntExact(booksFromCookieIds.size());
    }

    @ModelAttribute(name = "status")
    public String userAuthorityStatus()  {
         if (userRegister.getAuthenticationStatus()) {
            return "authorized";
        } else {
            return "unauthorized";
        }
    }

    @ModelAttribute(name = "authorizedUserName")
    public String authorizedUserName()  {
        if (userRegister.getAuthenticationStatus()) {
            return userRegister.getCurrentUser().getName();
        } else {
            return "";
        }
    }

    @ModelAttribute(name = "authorizedUserBalance")
    public Integer authorizedUserBalance()  {
        if (userRegister.getAuthenticationStatus()) {
            return userRegister.getCurrentUser().getBalance();
        } else {
            return 0;
        }
    }



}
