package com.bookshop.BookShopApp.security;

import com.bookshop.BookShopApp.services.JwtGenerate;
import com.bookshop.BookShopApp.services.JwtProvider;
import com.bookshop.BookShopApp.structure.user.BookstoreUserDetails;
import com.bookshop.BookShopApp.services.BookstoreUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JwtProvider jwtProvider;
    private final JwtBlackListToken jwtBlackListToken;
    private final JwtGenerate jwtGenerate;

    @Autowired
    public JwtRequestFilter(BookstoreUserDetailsService bookstoreUserDetailsService, JwtProvider jwtProvider, JwtBlackListToken jwtBlackListToken, JwtGenerate jwtGenerate) {
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtProvider = jwtProvider;
        this.jwtBlackListToken = jwtBlackListToken;
        this.jwtGenerate = jwtGenerate;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException, ExpiredJwtException {
        String token = null;
        String refresh = null;
        String username;
        Cookie[] cookies = httpServletRequest.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    token = cookie.getValue();
                }
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }

            if ((token !=null) & (refresh!=null)) {
                if ((jwtProvider.isAccessTokenExpired(token)) & (!jwtProvider.isRefreshTokenExpired(refresh)) & (!jwtBlackListToken.isTokenInBlackList(refresh)) & (!jwtBlackListToken.isTokenInBlackList(token))) {
                    JwtResponse response = jwtGenerate.getAccessToken(refresh);
                    if (response.getError().isEmpty()) {
                        token = response.getAccessToken();
                        Cookie cookieNewAccessToken = new Cookie("token", token);
                        cookieNewAccessToken.setPath("/");
                        cookieNewAccessToken.setMaxAge(2592000);
                        httpServletResponse.addCookie(cookieNewAccessToken);
                    }
                }
                if ((!jwtProvider.isAccessTokenExpired(token)) & (jwtProvider.isRefreshTokenExpired(refresh)) & (!jwtBlackListToken.isTokenInBlackList(refresh)) & (!jwtBlackListToken.isTokenInBlackList(token))) {
                    JwtResponse response = jwtGenerate.refresh(refresh);
                    if (response.getError().isEmpty()) {
                        Cookie cookieNewAccessToken = new Cookie("refresh", response.getRefreshToken());
                        cookieNewAccessToken.setPath("/");
                        cookieNewAccessToken.setMaxAge(2592000);
                        httpServletResponse.addCookie(cookieNewAccessToken);
                    }
                }
                try {
                    final Claims claims = jwtProvider.getAccessClaims(token);
                    username = claims.getSubject();
                } catch (JwtException e) {
                    username = null;
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    BookstoreUserDetails userDetails = (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(username);
                    if ((jwtProvider.validateAccessToken(token)) & (!jwtBlackListToken.isTokenInBlackList(token))) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            }
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
