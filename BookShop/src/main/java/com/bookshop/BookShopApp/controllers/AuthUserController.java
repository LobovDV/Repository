package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.security.JwtRequest;
import com.bookshop.BookShopApp.security.JwtResponse;
import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.services.SmsService;
import com.bookshop.BookShopApp.services.UserRegister;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.User;
import com.bookshop.BookShopApp.structure.user.UserContact;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Controller
public class AuthUserController {

    private final UserRegister userRegister;
    private final UserService userService;
    private final BookService bookService;
    private final SmsService smsService;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthUserController(UserRegister userRegister, UserService userService, BookService bookService, SmsService smsService, JavaMailSender javaMailSender, PasswordEncoder passwordEncoder) {
        this.userRegister = userRegister;
        this.userService = userService;
        this.bookService = bookService;
        this.smsService = smsService;
        this.javaMailSender = javaMailSender;
        this.passwordEncoder = passwordEncoder;
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

    public HashMap<String, String> sendAuthorisationCode(String code, ContactType contactType, String contact){
        HashMap<String, String> response = new HashMap<>();
        response.put("result", "true");
        if (contactType == ContactType.PHONE) {
            try {
                code = smsService.sendSecretCodeSms(contact, code);
                if (code.isEmpty()) { throw new Exception(); }
            } catch (Exception e) {
                //Раскоментировать когда заработает смс-шлюз
//                response.put("result", "false");
//                response.put("error", "Ошибка SMS-шлюза.");
//                return response;
            }
        } else {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("bookshop2022@mail.ru");
            message.setTo(contact);
            message.setSubject("BookShop email verification!");
            message.setText("Verification code is: " + code + " generate at: " + LocalDateTime.now() + "  and valid 3 minutes");
            javaMailSender.send(message);
        }
        return response;
    }

    @ApiOperation("operation confirmation user phone or e-mail, parameters contact - phone or e-mail")
    @PostMapping("/api/requestContactConfirmation")
    @ResponseBody
    public HashMap<String, Object> handleRequestContactConfirmation(@RequestBody JwtRequest jwtRequest, @CookieValue(name = "bookShop",  required = false) String bookShop) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("result", true);
        String code = smsService.generateCode();
        response.put("code", code);
        int userId = userService.getUserIdFromCookie(bookShop);
        ContactType contactType = jwtRequest.getContact().contains("@") ? ContactType.EMAIL : ContactType.PHONE;
        UserContact userContact = userService.getUserContactByContact(jwtRequest.getContact());

        if ((jwtRequest.getCode().equals("registration")) & (userContact == null)) {
            if (userService.getUserById(userId) == null) {
                userId = userService.newUser("Anonymous");
                Cookie cookie = new Cookie("bookShop", String.valueOf(userId));
                cookie.setPath("/");
                cookie.setMaxAge(2592000);
            }
            userService.addUserContact(userId, jwtRequest.getContact(), contactType, code, LocalDateTime.now());
            userContact = userService.getUserContactByContact(jwtRequest.getContact());
        }
        if(userContact != null) {
            HashMap<String, String>  sendCodeResult = sendAuthorisationCode(code, contactType, jwtRequest.getContact());
            if (!sendCodeResult.get("result").equals("true")) {
                response.put("result", false);
                response.put("error", sendCodeResult.get("error"));
                //Строка для отладки. Убрать когда акивируется СМС-шлюз.
                response.put("code", code);
                return response;
            }
        }


        if (jwtRequest.getCode().equals("login")) {
            if (userContact != null) {
                userService.updateUserLoginVerificationCode(code, LocalDateTime.now().plusMinutes(3), userContact.getUserId());
                return response;
            } else {
                response.put("result", false);
                response.put("error", "Пользователь не найден.");
            }
        }

        if (jwtRequest.getCode().equals("registration")) {
            if (userContact != null) {
                if (userContact.getUserId() == userId) {
                    if (userContact.getApproved() != 1) {
                        userService.updateUserContactCodeAndTime(code, LocalDateTime.now().plusMinutes(3), 0, userContact.getId());
                    } else {
                        userService.updateUserLoginVerificationCode(code, LocalDateTime.now(), userContact.getUserId());
                    }
                } else {
                    response.put("result", false);
                    response.put("error", "Указанный контакт уже зарегистрирован в базе");
                    return response;
                }
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
            if ((passwordEncoder.matches(JWTRequest.getCode(), userContact.getCode()) & (LocalDateTime.now().isBefore(userContact.getCodeTime())))) {
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

    @PostMapping("/api/confirmProfile")
    public ModelAndView handleApproveProfile(RegistrationForm registrationForm, @CookieValue(name = "bookShop", required = false) String bookShop, RedirectAttributes attributes, Model model) {

        User currentUser = userRegister.getCurrentUser();
        int userIdFromCookie = userService.getUserIdFromCookie(bookShop);
        User user1 = userService.getBookstoreUserByContact(registrationForm.getPhone(), 0);
        String phoneChange = user1.getId() == userIdFromCookie ? "phone=1" : "phone=0";
        User user2 = userService.getBookstoreUserByContact(registrationForm.getEmail(), 1);
        String mailChange = user2.getId() == userIdFromCookie ? "&mail=1" : "&mail=0";
        if ((phoneChange.equals("phone=0")) & (mailChange.equals("&mail=0")) & (currentUser.getName().equals(registrationForm.getName()))) {
            attributes.addFlashAttribute("profileAttribute", "Изменений не найдено!");
            return new ModelAndView("redirect:/profile");
        }
        userService.updateUserName(registrationForm.getName(), userIdFromCookie);
        String confirmProfileUrl = "http://localhost:8085/approveProfile?"+phoneChange+mailChange+"&userId="+currentUser.getId()+"&newContactUserId="+userIdFromCookie;
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String htmlMsg = "<h3>To confirm save your profile please open the URL:</h3><br><a href="+confirmProfileUrl+">Подтвердить изменения профайла<a/>";
        try {
            helper.setText(htmlMsg, true); // Use this or above line.
            helper.setTo(registrationForm.getEmail());
            helper.setSubject("BookShop: Profile edit verification!");
            helper.setFrom("bookshop2022@mail.ru");
        } catch (MessagingException e) {
            model.addAttribute("sendMessageConfirm", "Сбой отправки на E-Mail ссылки для подтверждения изменений");
        }
        javaMailSender.send(mimeMessage);
        model.addAttribute("sendMessageConfirm", "Вам на E-Mail направлена ссылка для подтверждения изменений");
        model.addAttribute("currentUser", currentUser);
        return new ModelAndView("/profile");
    }

    @GetMapping("/approveProfile")
    public ModelAndView handleProfileConfirm(@RequestParam("phone") Integer phone, @RequestParam("mail") Integer mail,
                                             @RequestParam("userId") Integer userId, @RequestParam("newContactUserId") Integer newContactUserId, RedirectAttributes attributes) {
        User tempUser = userService.getUserById(newContactUserId);
        userService.updateUserName(tempUser.getName(), userId);
        userService.updateUserName("Anonymous", newContactUserId);

        if (phone > 0) { userService.removeContactByUserIdAndType(userId, 0); }
        if (mail > 0) { userService.removeContactByUserIdAndType(userId, 1);}
        userService.updateUserContactUserId(userId, newContactUserId);
        attributes.addFlashAttribute("profileAttribute", "Success changed!");
        return new ModelAndView("redirect:/profile");
    }

    @PostMapping("/reg")
    public String handleUserRegistration(RegistrationForm registrationForm, Model model, @CookieValue(name = "bookShop", required = false) String bookShop) {
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
    public JwtResponse  handleLogin(@RequestBody JwtRequest jwtRequest, HttpServletResponse httpServletResponse, @CookieValue(name = "bookShop", required = false) String bookShop) {
        int contactType = jwtRequest.getContact().contains("@") ? 1 : 0;
        User user = userService.getBookstoreUserByContact(jwtRequest.getContact(), contactType);
        if (!LocalDateTime.now().isBefore(user.getCodeTime())) {
            return new JwtResponse(false, "", "", "Ошибка авторизации - срок действия кода истек");
        }
        JwtResponse loginResponse = userRegister.jwtLogin(jwtRequest);
        boolean result = loginResponse.isResult();
        if (result) {
            Cookie cookie = new Cookie("token", loginResponse.getAccessToken());
            cookie.setPath("/");
            cookie.setMaxAge(2592000);
            Cookie cookieRefresh = new Cookie("refresh", loginResponse.getRefreshToken());
            cookieRefresh.setPath("/");
            cookieRefresh.setMaxAge(2592000);
            httpServletResponse.addCookie(cookie);
            httpServletResponse.addCookie(cookieRefresh);
            Integer userFromCookieId = userService.getUserIdFromCookie(bookShop);
            if (userFromCookieId > 0) {
                userService.updateBook2UserUserId(userFromCookieId, user.getId());
            }
        }
        return loginResponse;
    }

    @GetMapping("/my")
    public String handleMy(Model model) {
        int userId = userRegister.getCurrentUser().getId();
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
    public String handleProfile(RegistrationForm registrationForm, Model model, @ModelAttribute("payAttribute") String payAttribute, @ModelAttribute("profileAttribute") String profileAttribute) {
//        User user = userRegister.getCurrentUser();
        User user = userService.getUserById(userRegister.getCurrentUser().getId());
        List<UserContact> userContacts = userService.getUserContactsByUserId(user.getId());
        String userEmail = "";
        String userPhone = "";
        for (UserContact contact: userContacts) {
            if (contact.getContact().contains("@")) {userEmail = contact.getContact();}
            if (contact.getContact().contains("+")) {userPhone = contact.getContact();}
        }
        if (user == null ) {
            System.out.println("user not found");
        }
        registrationForm = new RegistrationForm();
        registrationForm.setName(user.getName());
        registrationForm.setEmail(userEmail);
        registrationForm.setPhone(userPhone);
        model.addAttribute("currentUser", user);
        model.addAttribute("registrationForm", registrationForm);
//        model.addAttribute("userEmail", userEmail);
//        model.addAttribute("userPhone", userPhone);
        model.addAttribute("payStatus", payAttribute);
        model.addAttribute("changeProfileStatus", profileAttribute);

        model.addAttribute("transactions", userService.getPageOfTransactions(user.getId(), 0, 5).getContent());


        return "profile";
    }

}
