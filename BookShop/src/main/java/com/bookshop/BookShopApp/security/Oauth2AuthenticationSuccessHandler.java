package com.bookshop.BookShopApp.security;

import com.bookshop.BookShopApp.annotation.SuccessLogin;
import com.bookshop.BookShopApp.data.Book2UserRepository;
import com.bookshop.BookShopApp.data.UserContactRepository;
import com.bookshop.BookShopApp.data.UserRepository;
import com.bookshop.BookShopApp.services.BookstoreUserDetailsService;
import com.bookshop.BookShopApp.services.JwtProvider;
import com.bookshop.BookShopApp.util.AdditionalMethod;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.BookstoreUserDetails;
import com.bookshop.BookShopApp.structure.user.User;
import com.bookshop.BookShopApp.structure.user.UserContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class Oauth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtRefreshStorage jwtRefreshStorage;
    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JwtProvider jwtProvider;
    private final Book2UserRepository book2UserRepository;

    @Autowired
    public Oauth2AuthenticationSuccessHandler(JwtRefreshStorage jwtRefreshStorage, UserRepository userRepository, UserContactRepository userContactRepository, BookstoreUserDetailsService bookstoreUserDetailsService, JwtProvider jwtProvider, Book2UserRepository book2UserRepository) {
        this.jwtRefreshStorage = jwtRefreshStorage;
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtProvider = jwtProvider;
        this.book2UserRepository = book2UserRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    @SuccessLogin
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");



        User user = userRepository.findBookstoreUserByContact(email, 1);
        if (user == null) {
            user = new User();
            user.setBalance(0);
            user.setRegTime(LocalDateTime.now());
            user.setName(name);
            user.setHash(AdditionalMethod.createMD5Hash(name + LocalDateTime.now()));
            user.setRoles("USER");
            userRepository.save(user);
            int userId =  user.getId();

            UserContact userContact = new UserContact();
            userContact.setUserId(userId);
            userContact.setContact(email);
            userContact.setType(ContactType.EMAIL);
            userContact.setApproved((short) 1);
            userContact.setCode("Google");
            userContact.setCodeTime(LocalDateTime.now());
            userContact.setCodeTrails(0);
            userContactRepository.save(userContact);

        }

        Cookie[] cookies = httpServletRequest.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("bookShop")) {
                Integer userFromCookieId = 0;
                if ((cookie.getValue() != null) & (cookie.getValue() !="")){
                    if (userRepository.findUserById(Integer.valueOf(cookie.getValue())) != null) {
                        userFromCookieId = Integer.parseInt(cookie.getValue());
                    }
                }
                if (userFromCookieId > 0) {
                    book2UserRepository.modifyUserId(userFromCookieId, user.getId());
                }
            }
        }

        BookstoreUserDetails userDetails = (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(email);
        final String accessToken = jwtProvider.generateAccessToken(user, userDetails);
        final String refreshToken = jwtProvider.generateRefreshToken(userDetails);
        jwtRefreshStorage.addTokenToStorage(refreshToken, oauthUser.getAttribute("email"));
        Cookie cookie = new Cookie("token", accessToken);
        cookie.setPath("/");
        cookie.setMaxAge(2592000);
        Cookie cookieRefresh = new Cookie("refresh", refreshToken);
        cookieRefresh.setPath("/");
        cookieRefresh.setMaxAge(2592000);

        httpServletResponse.addCookie(cookie);
        httpServletResponse.addCookie(cookieRefresh);
        Authentication authenticationNew = new UsernamePasswordAuthenticationToken(email,"");
        SecurityContextHolder.setContext(new SecurityContextImpl(authenticationNew));

        httpServletResponse.sendRedirect("/my");

    }

}
