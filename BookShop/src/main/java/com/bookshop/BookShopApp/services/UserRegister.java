package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.security.ContactConfirmation;
import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.structure.user.BookstoreUserDetails;
import com.bookshop.BookShopApp.data.UserRepository;
import com.bookshop.BookShopApp.structure.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@Service
public class UserRegister {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JWTUtil jwtUtil;


    @Autowired
    public UserRegister(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                        BookstoreUserDetailsService bookstoreUserDetailsService, JWTUtil jwtUtil ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public String registerNewUser(RegistrationForm registrationForm, String bookShop) throws NoSuchAlgorithmException {
        int userFromCookieId = 0;
        if ((bookShop != null) & (bookShop != "")) {
            userFromCookieId = Integer.parseInt(bookShop);
        }
        int userId = userService.newUser(registrationForm.getName());
        userService.updateUserContactUserId(userId, userFromCookieId);
        return "";
    }

//    public HashMap<String, Object> login(ContactConfirmation contactConfirmation) {
//        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(contactConfirmation.getContact(), contactConfirmation.getCode());
//
//        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        HashMap<String, Object> response = new HashMap<>();
//        response.put("result", true);
//        return response;
//    }

    public HashMap<String, Object> jwtLogin(ContactConfirmation contactConfirmation) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(contactConfirmation.getContact(), contactConfirmation.getCode()));
        BookstoreUserDetails userDetails =
                (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(contactConfirmation.getContact());

        String jwtToken = jwtUtil.generateToken(userDetails);
        HashMap<String, Object> response = new HashMap<>();
        response.put("result",  jwtToken);
        return response;
    }


    public User getCurrentUser() {
        BookstoreUserDetails userDetails = (BookstoreUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public boolean getAuthenticationStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
}
