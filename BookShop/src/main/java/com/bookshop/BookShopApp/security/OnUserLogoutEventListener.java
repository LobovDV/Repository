package com.bookshop.BookShopApp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OnUserLogoutEventListener implements ApplicationListener<UserLogoutEvent> {

    private final BlackListToken blackListToken;

    @Autowired
    public OnUserLogoutEventListener(BlackListToken blackListToken) {
        this.blackListToken = blackListToken;
    }

    @Override
    public void onApplicationEvent(UserLogoutEvent event) {
        if (null != event) {
            blackListToken.addTokenToBlackList(event.getToken());
        }
    }
}
