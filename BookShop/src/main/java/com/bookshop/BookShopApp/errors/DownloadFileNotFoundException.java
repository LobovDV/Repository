package com.bookshop.BookShopApp.errors;

public class DownloadFileNotFoundException extends Exception{

    private String slug;

    public DownloadFileNotFoundException(String message, String slug) {
        super(message);
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
