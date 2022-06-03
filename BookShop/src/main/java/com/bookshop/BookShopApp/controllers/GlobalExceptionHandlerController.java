package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.errors.BookstoreApiWrongParameterException;
import com.bookshop.BookShopApp.errors.DownloadFileNotFoundException;
import com.bookshop.BookShopApp.errors.EmptySearchException;
import com.bookshop.BookShopApp.errors.UploadFileException;
import com.bookshop.BookShopApp.data.ApiResponse;
import com.bookshop.BookShopApp.structure.book.Book;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(EmptySearchException.class)
    public String handleEmptySearchException(EmptySearchException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("searchError",e);
        return "redirect:/";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Book>> handleMissingServletRequestParameterException(Exception exception){
        return new ResponseEntity<>(new ApiResponse<Book>(HttpStatus.BAD_REQUEST, "Missing required parameters",
                exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BookstoreApiWrongParameterException.class, NumberFormatException.class})
    public ResponseEntity<ApiResponse<Book>> handleBookstoreApiWrongParameterException(Exception exception){
        return new ResponseEntity<>(new ApiResponse<Book>(HttpStatus.BAD_REQUEST, "Bad parameter value...",exception)
                ,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DownloadFileNotFoundException.class)
    public String handleDownloadFileNotFoundException(DownloadFileNotFoundException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("downloadError",e);
        return "redirect:/books/"+e.getSlug();
    }

    @ExceptionHandler(UploadFileException.class)
    public String handleUploadFileException(UploadFileException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("uploadError",e);
        return "redirect:/books/"+e.getSlug();
    }

//    @ExceptionHandler(JwtException.class)
//    public String handleExpiredJwtException(ExpiredJwtException e){
//        return "redirect:/login";
//    }
}
