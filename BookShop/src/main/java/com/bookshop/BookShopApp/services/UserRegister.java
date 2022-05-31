package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.UserContactRepository;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class UserRegister {

    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JWTUtil jwtUtil;


    @Autowired
    public UserRegister(UserRepository userRepository, UserContactRepository userContactRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                        BookstoreUserDetailsService bookstoreUserDetailsService, JWTUtil jwtUtil ) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
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

        User user = new User();
        user.setBalance(0);
        user.setRegTime(LocalDateTime.now());
        user.setName(registrationForm.getName());
        user.setHash(createMD5Hash(registrationForm.getName() + LocalDateTime.now()));
        userRepository.save(user);
        int userId = user.getId();

        userContactRepository.modifyUserContactUserId(userId, userFromCookieId);
        return "";
    }

    public HashMap<String, Object> jwtLogin(ContactConfirmation contactConfirmation) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("result",  false);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(contactConfirmation.getContact(), contactConfirmation.getCode()));
            BookstoreUserDetails userDetails =
                    (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(contactConfirmation.getContact());

            String jwtToken = jwtUtil.generateToken(userDetails);
            response.put("result", jwtToken);
//            System.out.println(jwtUtil.extractExpiration(jwtToken));
        } catch (AuthenticationException e) {
            response.put("result", "error");
        }
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

    public String createMD5Hash(String input)  {

        try {
            String hashtext = null;
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Compute message digest of the input
            byte[] messageDigest = md.digest(input.getBytes());
            hashtext = convertToHex(messageDigest);
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToHex(byte[] messageDigest) {
        StringBuilder builder = new StringBuilder();
        for (Byte b : messageDigest) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }
}
