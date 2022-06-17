package com.bookshop.BookShopApp.security;

import com.bookshop.BookShopApp.services.JwtProvider;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtBlackListToken {

    private final ExpiringMap<String, Date> expiringMap = ExpiringMap.builder().variableExpiration().maxSize(1000).build();
    private final JwtProvider jwtProvider;

    @Autowired
    public JwtBlackListToken(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public boolean addTokenToBlackList(String token, int type) {
        boolean expired = type == 1 ? jwtProvider.isAccessTokenExpired(token) : jwtProvider.isRefreshTokenExpired(token);
        if ((!expiringMap.containsKey(token)) & (!expired)) {
            Date tokenExpiryDate = type == 1 ? jwtProvider.extractAccessExpiration(token) : jwtProvider.extractRefreshExpiration(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);
            expiringMap.put(token, tokenExpiryDate, ttlForToken, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    public boolean isTokenInBlackList(String token) {
        return expiringMap.containsKey(token);
    }

    private long getTTLForToken(Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }

}
