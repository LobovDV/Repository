package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.errors.DownloadFileNotFoundException;
import com.bookshop.BookShopApp.errors.UploadFileException;
import com.bookshop.BookShopApp.services.Book2UserService;
import com.bookshop.BookShopApp.services.ResourceStorage;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.book.review.BookScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;


import java.io.IOException;

@Controller
@RequestMapping("/books")
public class BooksController {


    private final BookService bookService;
    private final ResourceStorage storage;
    private final UserService userService;
    private final Book2UserService book2UserService;



    @Autowired
    public BooksController(BookService bookService, ResourceStorage storage, UserService userService, Book2UserService book2UserService) {
        this.bookService = bookService;
        this.storage = storage;
        this.userService = userService;
        this.book2UserService = book2UserService;
    }


   @GetMapping("/{slug}")
    public String bookPage(@PathVariable("slug") String slug, @CookieValue(name = "bookShop",
           required = false) String bookShop, Model model) throws NoSuchAlgorithmException {
        Book book = bookService.getBookBySlug(slug);
        model.addAttribute("slugBook", book);
        model.addAttribute("buttonBookCartedName", "Купить");
        model.addAttribute("buttonBookPostponedName", "Отложить");
        Integer userId = userService.getCurrentUserId(bookShop);
        model.addAttribute("buttonBookCartedName", book2UserService.getBook2UserByUserIdBookIdAndStatus(userId, book.getId(), "CART") != null ? "В корзине" : "Купить");
        model.addAttribute("buttonBookPostponedName", book2UserService.getBook2UserByUserIdBookIdAndStatus(userId, book.getId(), "KEPT") != null ? "Отложена" : "Отложить");
        model.addAttribute("ratingList", bookService.getBookRatingTable(book.getId()));
        model.addAttribute("scoreCount", bookService.getCountBookScore(book.getId()));
        BookScore bookScore = bookService.getBookScoreByBookIdAndUserId(book.getId(), userId);
        int score = 0;
        String ratingClass = "Rating Rating_input";
        if ( bookScore != null) {
            score = bookScore.getScore();
            ratingClass = ratingClass.concat(" Rating_inputClick");
        }
       model.addAttribute("score", score);
       model.addAttribute("ratingClass", ratingClass);
       model.addAttribute("ratingArray", new byte[]{1,1,1,1,1});

       model.addAttribute("bookReviewList", bookService.getAllReviewsWithCountLikesByBookId(book.getId()));
        return "/books/slug";
    }

    @PostMapping("/{slug}/img/save")
    public String saveNewBookImage(@RequestParam("file") MultipartFile file, @PathVariable("slug")String slug) throws IOException, UploadFileException {
        String savePath = storage.saveNewBookImage(file,slug);
        Book bookToUpdate = bookService.getBookBySlug(slug);
        bookToUpdate.setImage(savePath);
        bookService.updateBook(bookToUpdate);
        return "redirect:/books/"+slug;
    }

    @GetMapping("/download/{hash}")
    public ResponseEntity<ByteArrayResource> bookFile(@PathVariable("hash")String hash) throws IOException, DownloadFileNotFoundException {
        Path path = storage.getBookFilePath(hash);
        Logger.getLogger(this.getClass().getSimpleName()).info("book file path: "+path);

        MediaType mediaType = storage.getBookFileMime(hash);
        Logger.getLogger(this.getClass().getSimpleName()).info("book file mime type: "+mediaType);

        if (Files.exists(storage.getCompleteBookFilePath(hash))) {
            byte[] data = storage.getBookFileByteArray(hash);
            Logger.getLogger(this.getClass().getSimpleName()).info("book file data len: " + data.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
                    .contentType(mediaType)
                    .contentLength(data.length)
                    .body(new ByteArrayResource(data));
        } else {
            throw new DownloadFileNotFoundException("Sorry, file not found", bookService.getBookByFileHash(hash).getSlug());
        }
    }

}
