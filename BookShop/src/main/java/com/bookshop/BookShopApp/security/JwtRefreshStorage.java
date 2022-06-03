package com.bookshop.BookShopApp.security;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtRefreshStorage {

    private final Map<String, String> refreshMap = new HashMap<>();


    public void addTokenToStorage(String token, String login) {
        if (!refreshMap.containsKey(login)){
            refreshMap.put(token, login);
        }
    }

    public boolean isTokenInStorage(String token) {
        return refreshMap.containsKey(token);
    }

    public void removeFromStorage(String token) {
        refreshMap.remove(token);
    }
}
