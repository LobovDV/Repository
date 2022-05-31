package com.bookshop.BookShopApp.security;

import com.bookshop.BookShopApp.services.JWTUtil;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class BlackListToken {

//    private final ExpiringMap expiringMap;
    private final ExpiringMap<String, Date> expiringMap = ExpiringMap.builder().variableExpiration().build();
    private final JWTUtil jwtUtil;

    @Autowired
    public BlackListToken(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
//        this.expiringMap = ExpiringMap.builder()
//                .variableExpiration()
//                .maxSize(1000)
//                .build();

    }

    public void addTokenToBlackList(String token) {
//        String token = event.getToken();
//        if (expiringMap.containsKey(token)) {
//
//
//        } else
        if (!expiringMap.containsKey(token)){
            Date tokenExpiryDate = jwtUtil.extractExpiration(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);

            expiringMap.put(token, tokenExpiryDate, ttlForToken, TimeUnit.SECONDS);
        }
    }

    public boolean isTokenInBlackList(String token) {
        return expiringMap.containsKey(token);
    }
//
//    public OnUserLogoutSuccessEvent getLogoutEventForToken(String token) {
//        return expiringMap.get(token);
//    }

    private long getTTLForToken(Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}
