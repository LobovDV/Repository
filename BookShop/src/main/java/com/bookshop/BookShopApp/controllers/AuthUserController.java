package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.security.JwtRequest;
import com.bookshop.BookShopApp.security.JwtResponse;
import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.services.UserRegister;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.User;
import com.bookshop.BookShopApp.structure.user.UserContact;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Controller
public class AuthUserController {

    private final UserRegister userRegister;
    private final UserService userService;
    private final BookService bookService;

    @Autowired
    public AuthUserController(UserRegister userRegister, UserService userService, BookService bookService) {

        this.userRegister = userRegister;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/signin")
    public String handleSignin() {
        return "signin";
    }

    @GetMapping("/signup")
    public String handleSignUp(Model model) {
        model.addAttribute("regForm", new RegistrationForm());
        model.addAttribute("addError", "");
        return "signup";
    }

    @ApiOperation("operation confirmation user phone or e-mail, parameters contact - phone or e-mail")
    @PostMapping("/api/requestContactConfirmation")
    @ResponseBody
    public HashMap<String, Object> handleRequestContactConfirmation(@RequestBody JwtRequest JWTRequest, @CookieValue(name = "bookShop",  required = false) String bookShop)  {
        HashMap<String, Object> response = new HashMap<>();
        response.put("result", true);
        String code = "123 123";
        int userId = userService.getUserIdFromCookie(bookShop);
        ContactType contactType = JWTRequest.getContact().contains("@") ? ContactType.EMAIL : ContactType.PHONE;
        UserContact userContact = userService.getUserContactByContact(JWTRequest.getContact());

        if (JWTRequest.getCode().equals("login")) {
            if (userContact != null) {
                userService.updateUserLoginVerificationCode(code, userContact.getUserId());
                return response;
            } else {
                response.put("result", false);
                response.put("error", "Пользователь не найден.");
            }
        }

        if (JWTRequest.getCode().equals("registration")) {
            if (userContact != null) {
                if (userContact.getUserId() == userId) {
                    if (userContact.getApproved() != 1) {
                        userService.updateUserContactCodeAndTime(code, LocalDateTime.now(), 0, userContact.getId());
                    } else {
                        userService.updateUserLoginVerificationCode(code, userContact.getUserId());
                    }
                } else {
                    response.put("result", false);
                    response.put("error", "Указанный контакт уже зарегистрирован в базе");
                    return response;
                }
            } else {
                if (userService.getUserById(userId) == null) {
                    userId = userService.newUser("Anonymous");
                    Cookie cookie = new Cookie("bookShop", String.valueOf(userId));
                    cookie.setPath("/");
                    cookie.setMaxAge(2592000);
                }
                userService.addUserContact(userId, JWTRequest.getContact(), contactType, code, LocalDateTime.now());
            }
        }
        return response;
    }

    @ApiOperation("approve user contact: phone or e-mail, parameters contact and code")
    @PostMapping("/api/approveContact")
    @ResponseBody
    public HashMap<String, Object> handleApproveContact(@RequestBody JwtRequest JWTRequest, @CookieValue(name = "bookShop", required = false) String bookShop) {
        int userId = Integer.parseInt(bookShop);
        HashMap<String, Object> response = new HashMap<>();
        response.put("result", true);
        UserContact userContact = userService.getUserContactByContact(JWTRequest.getContact());
        if (userContact != null) {
            if (userContact.getCode().equals(JWTRequest.getCode())) {
                userService.updateUserContactApproved(1, userContact.getId());
            } else {
                response.put("error", "Код не верен.");
                response.put("result", false);
            }
        } else {
            response.put("error", "Контакт не найден.");
            response.put("result", false);
        }
        return response;
    }

    @PostMapping("/reg")
    public String handleUserRegistration(RegistrationForm registrationForm, Model model, @CookieValue(name = "bookShop",
            required = false) String bookShop) {
        User user = userRegister.registerNewUser(registrationForm, bookShop);
        if (user != null) {
            model.addAttribute("regOk", true);
            return "signin";
        } else {
            model.addAttribute("regForm", registrationForm);
            model.addAttribute("addError", "User creation error");
            return "signup";
        }
    }

    @PostMapping("/login")
    @ResponseBody
    public JwtResponse  handleLogin(@RequestBody JwtRequest jwtRequest, HttpServletResponse httpServletResponse) {
        JwtResponse loginResponse = userRegister.jwtLogin(jwtRequest);
        String result = loginResponse.getResult();
        if (!result.equals("error")) {
            Cookie cookie = new Cookie("token", loginResponse.getAccessToken());
            cookie.setPath("/");
            cookie.setMaxAge(2592000);
            Cookie cookieRefresh = new Cookie("refresh", loginResponse.getRefreshToken());
            cookieRefresh.setPath("/");
            cookieRefresh.setMaxAge(2592000);

            httpServletResponse.addCookie(cookie);
            httpServletResponse.addCookie(cookieRefresh);
        }
        return loginResponse;
    }

    @GetMapping("/my")
    public String handleMy(Model model) {
        int userId = userRegister.getCurrentUser().getId();
        userService.updateUserLoginVerificationCode("", userId);
        List<Book> currentUserBooks = bookService.getBooksByUserIdAndStatus(userId, "PAID");
        model.addAttribute("myBooks", currentUserBooks);
        return "my";
    }

    @GetMapping("/myarchive")
    public String handleMyArchive(Model model) {
        int userId = userRegister.getCurrentUser().getId();
        List<Book> currentUserBooks = bookService.getBooksByUserIdAndStatus(userId, "ARCHIVED");
        model.addAttribute("myBooks", currentUserBooks);
        return "myarchive";
    }

    @GetMapping("/profile")
    public String handleProfile(Model model) {
        User user = userRegister.getCurrentUser();
        List<UserContact> usercontacts = userService.getUserContactsByUserId(user.getId());
        String userEmail = "";
        String userPhone = "";
        for (UserContact contact: usercontacts) {
            if (contact.getContact().contains("@")) {userEmail = contact.getContact();}
            if (contact.getContact().contains("+")) {userPhone = contact.getContact();}
        }
        if (user == null ) {
            System.out.println("user not found");
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userPhone", userPhone);
        return "profile";
    }

}
