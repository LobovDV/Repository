package com.bookshop.BookShopApp.security;

public class JwtResponse {

    private String result;
    private String accessToken;
    private String refreshToken;
    private String error;

    public JwtResponse(String result, String accessToken, String refreshToken, String error) {
        this.result = result;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.error = error;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
