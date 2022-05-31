package com.bookshop.BookShopApp.security;

import org.springframework.context.ApplicationEvent;

import java.util.Date;

public class UserLogoutEvent extends ApplicationEvent {


    private final String token;
    private final Date eventTime;

    public UserLogoutEvent(Object source, String token, Date eventTime) {
        super(source);
        this.token = token;
        this.eventTime = eventTime;
    }

    public String getToken() {
        return token;
    }
}
