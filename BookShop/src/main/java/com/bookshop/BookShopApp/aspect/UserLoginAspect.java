package com.bookshop.BookShopApp.aspect;

import com.bookshop.BookShopApp.security.JwtRequest;
import com.bookshop.BookShopApp.security.JwtResponse;
import com.bookshop.BookShopApp.services.UserService;
import org.aspectj.lang.JoinPoint;
import org.springframework.security.core.Authentication;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Aspect
@Component
public class UserLoginAspect {

    private final UserService userService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    public UserLoginAspect(UserService userService) {
        this.userService = userService;
    }

    @Pointcut(value = "execution(boolean com.bookshop.BookShopApp.security.JwtBlackListToken.*BlackList(..)) && args(token, type)")
    public void addTokenToBlackListMethod(String token, int type) {
    }

    @Pointcut(value = "execution(boolean com.bookshop.BookShopApp.security.JwtBlackListToken.*BlackList(..)) && args(token)")
    public void isTokenInBlackListMethod(String token) {
    }

    @Around(value = "addTokenToBlackListMethod(token, type)" , argNames = "pjp,token, type")
    public Object execForAddTokenToBlackListMethod(ProceedingJoinPoint pjp, String token, int type) {
        Object returnValue = null;
        try {
            returnValue = pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        String tokenType = type == 1 ? "mainToken " : "refreshToken ";
        if ((Boolean) returnValue == true) {
            logger.info(tokenType + token + " added to BlackList " + LocalDateTime.now());
        }
        return returnValue;
    }

    @Around(value = "isTokenInBlackListMethod(token)" , argNames = "pjp,token")
    public Object execForIsTokenInBlackListMethod(ProceedingJoinPoint pjp, String token) {
        Object returnValue = null;
        try {
            returnValue = pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if ((Boolean) returnValue == true) {
            logger.info("Authentication with token in BlackList " + token + LocalDateTime.now());
        }
        return returnValue;
    }

    @Around(value = "@annotation(com.bookshop.BookShopApp.annotation.SuccessLogin)")
    public Object userSuccessLogin(ProceedingJoinPoint pjp) throws NoSuchFieldException {

        Object returnValue = null;
        try {
            returnValue = pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        String userName;
        String login;
        Object[] parameters = pjp.getArgs();
        if (pjp.toString().contains("jwtLogin")) {
            JwtResponse jwtResponse = (JwtResponse) returnValue;
            if (!jwtResponse.getResult().equals("")) {
                JwtRequest jwtRequests = (JwtRequest) parameters[0];
                userName = " local " + userService.getBookstoreUserByContact(jwtRequests.getContact(), 0).getName();
                login = jwtRequests.getContact();
                logger.info("Success login user " + userName + " with login " + login + " " + LocalDateTime.now());
            } else {logger.info("Login Failed " + LocalDateTime.now());}
        } else {
            Authentication authentication = (Authentication) parameters[2];
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            login = oauthUser.getAttribute("email");
            userName = " oauth2 " + oauthUser.getAttribute("name");
            logger.info("Success login user " + userName + " with login " + login + " " + LocalDateTime.now());
        }

        return returnValue;
    }


    @Pointcut(value = "within(com.bookshop.BookShopApp.controllers.BookshopCartController)")
    public void allMethodsOfBookShopCartController() {
    }

    @After("allMethodsOfBookShopCartController()")
    public void allMethodsOfBookShopCartControllerAdvice(JoinPoint joinPoint) {
        if (!joinPoint.toString().contains("bookCart()")) {
            if (joinPoint.toShortString().contains("CartRequest")) {
                logger.info("User open Cart ");
            } else {
                logger.info("User open postponed ");
            }
        }
    }


}
