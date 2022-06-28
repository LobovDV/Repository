package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.UserRepository;
import com.bookshop.BookShopApp.security.JwtRefreshStorage;
import com.bookshop.BookShopApp.security.JwtResponse;
import com.bookshop.BookShopApp.structure.user.BookstoreUserDetails;
import com.bookshop.BookShopApp.structure.user.User;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtGenerate {

    private final JwtRefreshStorage jwtRefreshStorage;
    private final UserRepository userRepository;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JwtProvider jwtProvider;

    @Autowired
    public JwtGenerate(UserRepository userRepository, BookstoreUserDetailsService bookstoreUserDetailsService, JwtProvider jwtProvider, JwtRefreshStorage jwtRefreshStorage) {
        this.userRepository = userRepository;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtProvider = jwtProvider;
        this.jwtRefreshStorage = jwtRefreshStorage;
    }

    public JwtResponse getAccessToken(String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            if (jwtRefreshStorage.isTokenInStorage(refreshToken)) {
                int type = login.contains("@") ? 1 : 0;
                User user = userRepository.findBookstoreUserByContact(login, type);
                if (user != null) {
                    BookstoreUserDetails userDetails = (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(login);
                    final String accessToken = jwtProvider.generateAccessToken(user, userDetails);
                    return new JwtResponse(true, accessToken, null, "");
                } else {return new JwtResponse(false, null, null, "Пользователь не найден");}
            }
        }
        return new JwtResponse(false,null, null, "Ошибка валидации");
    }

    public JwtResponse refresh(String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            if (jwtRefreshStorage.isTokenInStorage(refreshToken)) {
                int type = login.contains("@") ? 1 : 0;
                User user = userRepository.findBookstoreUserByContact(login, type);
                if (user != null) {
                    BookstoreUserDetails userDetails = (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(login);
                    final String accessToken = jwtProvider.generateAccessToken(user, userDetails);
                    final String newRefreshToken = jwtProvider.generateRefreshToken(userDetails);
                    jwtRefreshStorage.addTokenToStorage(newRefreshToken, userDetails.getUsername());
                    jwtRefreshStorage.removeFromStorage(refreshToken);
                    return new JwtResponse(true, accessToken, newRefreshToken, null);
                } else {return new JwtResponse(false, null, null, "Пользователь не найден");}
            }
        }
        return new JwtResponse(false, null, null, "Невалидный JWT токен");
    }
}
