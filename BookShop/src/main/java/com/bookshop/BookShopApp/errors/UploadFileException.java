package com.bookshop.BookShopApp.errors;

public class UploadFileException extends Exception{

    private String slug;

    public UploadFileException(String message, String slug) {
        super(message);
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
