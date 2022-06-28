package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.services.*;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookshopCartController {

    @ModelAttribute(name = "bookCart")
    public List<Book> bookCart() {
        return new ArrayList<>();
    }


    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public BookshopCartController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping("/cart")
    public String handleCartRequest(String resultPay, @CookieValue(value = "bookShop", required = false) String bookShop, Model model) {

        model.addAttribute("isCartEmpty", true);
        Integer userId = userService.getCurrentUserId(bookShop);
        List<Book> currentUserBooks = bookService.getBooksByUserIdAndStatus(userId, "CART");
        model.addAttribute("bookCart", currentUserBooks);
        model.addAttribute("userId", userId);
        model.addAttribute("resultPay", resultPay);
        if (currentUserBooks.size() > 0) {
            model.addAttribute("cartPrice", currentUserBooks.stream().map(Book::priceWithDiscount).reduce(Integer::sum).get());
            model.addAttribute("cartPriceOld", currentUserBooks.stream().map(Book::getPrice).reduce(Integer::sum).get());
            model.addAttribute("isCartEmpty", false);
        }

        return "cart";
    }

    @GetMapping("/postponed")
    public String handlePostponedRequest(@CookieValue(value = "bookShop", required = false) String bookShop, Model model) {
        model.addAttribute("isPostponedEmpty", true);
        Integer userId = userService.getCurrentUserId(bookShop);
        List<Book> currentUserBooks = bookService.getBooksByUserIdAndStatus(userId, "KEPT");
        model.addAttribute("bookPostponed", currentUserBooks);
        if (currentUserBooks.size() > 0) {
            model.addAttribute("isPostponedEmpty", false);
            model.addAttribute("allIds", currentUserBooks.stream().map(book -> String.valueOf(book.getId())).collect(Collectors.joining(",", "[", "]")));
        }
        return "postponed";
    }



}
