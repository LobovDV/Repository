package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.UserContactRepository;
import com.bookshop.BookShopApp.security.JwtRefreshStorage;
import com.bookshop.BookShopApp.security.JwtRequest;
import com.bookshop.BookShopApp.security.JwtResponse;
import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.structure.user.BookstoreUserDetails;
import com.bookshop.BookShopApp.data.UserRepository;
import com.bookshop.BookShopApp.structure.user.User;
import com.bookshop.BookShopApp.util.AdditionalMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserRegister {

    private final JwtRefreshStorage jwtRefreshStorage;
    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JwtProvider jwtProvider;


    @Autowired
    public UserRegister(UserRepository userRepository, UserContactRepository userContactRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                        BookstoreUserDetailsService bookstoreUserDetailsService, JwtProvider jwtProvider, JwtRefreshStorage jwtRefreshStorage) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtProvider = jwtProvider;
        this.jwtRefreshStorage = jwtRefreshStorage;
    }

    public JwtResponse jwtLogin(JwtRequest JWTRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(JWTRequest.getContact(), JWTRequest.getCode()));
            BookstoreUserDetails userDetails =
                    (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(JWTRequest.getContact());
            int type = JWTRequest.getContact().contains("@") ? 1 : 0;
            User user = userRepository.findBookstoreUserByContact(JWTRequest.getContact(), type);
            final String accessToken = jwtProvider.generateAccessToken(user, userDetails);
            final String refreshToken = jwtProvider.generateRefreshToken(userDetails);
            jwtRefreshStorage.addTokenToStorage(refreshToken, userDetails.getUsername());
            return new JwtResponse(accessToken, accessToken, refreshToken, "");
        } catch (AuthenticationException e) {
            return new JwtResponse("error", "", "", "Ошибка авторизации");
        }
    }

    public String registerNewUser(RegistrationForm registrationForm, String bookShop) {
        int userFromCookieId = 0;
        if ((bookShop != null) & (bookShop != "")) {
            userFromCookieId = Integer.parseInt(bookShop);
        }

        User user = new User();
        user.setBalance(0);
        user.setRegTime(LocalDateTime.now());
        user.setName(registrationForm.getName());
        user.setHash(AdditionalMethod.createMD5Hash(registrationForm.getName() + LocalDateTime.now()));
        user.setRoles("USER");
        userRepository.save(user);
        int userId = user.getId();

        userContactRepository.modifyUserContactUserId(userId, userFromCookieId);
        return "";
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
