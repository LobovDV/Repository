package com.bookshop.BookShopApp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserLogoutHandler implements LogoutHandler {

    private final JwtBlackListToken jwtBlackListToken;

    @Autowired
    public UserLogoutHandler(JwtBlackListToken jwtBlackListToken) {
        this.jwtBlackListToken = jwtBlackListToken;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        SecurityContextHolder.clearContext();
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    jwtBlackListToken.addTokenToBlackList(cookie.getValue(), 1);
                }
                if (cookie.getName().equals("refresh")) {
                    jwtBlackListToken.addTokenToBlackList(cookie.getValue(), 2);
                }
            }
        }
    }
}
