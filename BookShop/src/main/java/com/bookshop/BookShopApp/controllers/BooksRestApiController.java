package com.bookshop.BookShopApp.controllers;
import com.bookshop.BookShopApp.data.ApiResponse;
import com.bookshop.BookShopApp.errors.BookstoreApiWrongParameterException;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.book.review.BookReview;
import com.bookshop.BookShopApp.structure.book.review.BookReviewLike;
import com.bookshop.BookShopApp.structure.book.review.BookScore;
import com.bookshop.BookShopApp.data.BooksPageDto;
import com.bookshop.BookShopApp.data.SearchWordDto;
import com.bookshop.BookShopApp.services.Book2UserService;
import com.bookshop.BookShopApp.services.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@Api("book data api")
public class BooksRestApiController {

    private final BookService bookService;
    private final Book2UserService book2UserService;
    private final UserService userService;
    private static final Integer MAX_BOOKS_ON_PAGE = 1000000000;


    @Autowired
    public BooksRestApiController(BookService bookService, Book2UserService book2UserService, UserService userService) {
        this.bookService = bookService;
        this.book2UserService = book2UserService;
        this.userService = userService;
    }

    @GetMapping("/books/recommended")
    @ApiOperation("operation to get recommended book list of bookshop, parameters offset, limit - Integer")
    @ResponseBody
    public BooksPageDto getRecommendedBooksPage(@RequestParam("offset") Integer offset,
                                                @RequestParam("limit") Integer limit) throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfRecommendedBooks(offset, limit).getContent());
            int count = bookService.getPageOfRecommendedBooks(0, MAX_BOOKS_ON_PAGE).getContent().size();
            booksPageDto.setCount(count);
            return booksPageDto;
        }
    }

    @GetMapping("/books/recent")
    @ApiOperation("operation to get recent book list of bookshop order by public date (descending), parameters from, to - date format dd-mm-yyyy, offset, limit - Integer")
    @ResponseBody
    public BooksPageDto getRecentBooksPage(@RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("offset") Integer offset,
                                           @RequestParam("limit") Integer limit) throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)) {
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            try {
                Date fromDate = !from.isEmpty() ? formatter.parse(from) : formatter.parse("01-01-1800");
                Date toDate = !to.isEmpty() ? formatter.parse(to) : new Date();
                BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfRecentBooks(fromDate, toDate, offset, limit).getContent());
                int count = bookService.getPageOfRecentBooks(fromDate, toDate, offset, limit).getContent().size();
                booksPageDto.setCount(count);
                return booksPageDto;
            } catch( ParseException e) {
                throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
            }
        }
    }

    @GetMapping("/books/popular")
    @ApiOperation("operation to get popular book list of bookshop order by popularity (descending), parameters offset, limit - Integer")
    @ResponseBody
    public BooksPageDto getPopularBooksPage(@RequestParam("offset") Integer offset,
                                            @RequestParam("limit") Integer limit) throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfPopularBooks(offset, limit).getContent());
            int count = bookService.getPageOfPopularBooks(0, MAX_BOOKS_ON_PAGE).getContent().size();
            booksPageDto.setCount(count);
            return booksPageDto;
        }
    }

    @GetMapping("/books/tags/{tagId}")
    @ApiOperation("operation to get book list of bookshop by tag id, parameters offset, limit - Integer; tagId - String")
    @ResponseBody
    public BooksPageDto getBooksByTagNextPage(@RequestParam("offset") Integer offset,
                                              @RequestParam("limit") Integer limit,
                                              @PathVariable(value = "tagId", required = true) Integer tagId) throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfBooksByTagId(tagId, offset, limit).getContent());
            int count = bookService.getPageOfBooksByTagId(tagId, 0, MAX_BOOKS_ON_PAGE).getContent().size();
            booksPageDto.setCount(count);
            return booksPageDto;
        }
    }

    @GetMapping("/books/genre/{genreId}")
    @ApiOperation("operation to get book list of bookshop by genre id, parameters offset, limit - Integer; genreId - String")
    @ResponseBody
    public BooksPageDto getBooksByGenreNextPage(@RequestParam("offset") Integer offset,
                                                @RequestParam("limit") Integer limit,
                                                @PathVariable(value = "genreId", required = true) Integer genreId) throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfBooksByGenreId(genreId, offset, limit).getContent());
            int count = bookService.getPageOfBooksByGenreId(genreId, 0, MAX_BOOKS_ON_PAGE).getContent().size();
            booksPageDto.setCount(count);
            return booksPageDto;
        }
    }

    @GetMapping("/books/author/{authorId}")
    @ApiOperation("operation to get book list of bookshop by author id, parameters offset, limit - Integer; authorId - String")
    @ResponseBody
    public BooksPageDto getBooksByAuthorNextPage(@RequestParam("offset") Integer offset,
                                                @RequestParam("limit") Integer limit,
                                                @PathVariable(value = "authorId", required = true) Integer authorId)  throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfBooksByAuthorId(authorId, offset, limit).getContent());
            int count = bookService.getPageOfBooksByAuthorId(authorId, 0, MAX_BOOKS_ON_PAGE).getContent().size();
            booksPageDto.setCount(count);
            return booksPageDto;
        }
    }

    @GetMapping("/books/search/{searchWord}")
    @ApiOperation("operation to get book list of bookshop by search parameter, parameters offset, limit - Integer; searchWorld - String")
    @ResponseBody
    public BooksPageDto getNextSearchPage(@RequestParam("offset") Integer offset,
                                          @RequestParam("limit") Integer limit,
                                          @PathVariable(value = "searchWord", required = false) SearchWordDto searchWordDto)  throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            BooksPageDto booksPageDto = new BooksPageDto(bookService.getPageOfSearchResultBooks(searchWordDto.getExample(), offset, limit).getContent());
            int count = bookService.getPageOfSearchResultBooks(searchWordDto.getExample(), 0, MAX_BOOKS_ON_PAGE).getContent().size();
            booksPageDto.setCount(count);
            return booksPageDto;
        }
    }

    @PostMapping("/changeBookStatus")
    @ApiOperation("operation to change book status, parameters bookIds Integer[], String status")
    @ResponseBody
    public HashMap<String, Object> handleChangeBookStatus(@RequestParam(value="booksIds[]") Integer[] booksIds, String status, @CookieValue(name = "bookShop",
            required = false) String bookShop ) throws NoSuchAlgorithmException {
        String errorMessage = "";

        HashMap<String, Object> result = new HashMap<>();
        result.put("result", true);
        Integer userId = userService.getCurrentUserId(bookShop);
        for (int i = 0; i < booksIds.length; i++) {
                if (status.equals("UNLINK")) {
                    if(!book2UserService.deleteByBookIdAndUserId(booksIds[i], userId)) {
                        errorMessage = "Ошибка удаления из Отложенных/Корзины";
                    }
                } else {
                    if (!book2UserService.updateByBookIdAnfUserId(booksIds[i], userId, status)) {
                        errorMessage = "Ошибка обновления статуса";
                    }
                }
                bookService.updateBookPopularity(booksIds[i]);
        }
        if (!errorMessage.equals("")) {
            result.replace("result", true, false);
            result.put("error", errorMessage);
        }

        return result;
    }


    @PostMapping("/rateBook")
    @ApiOperation("operation to change book rating, parameters bookId Integer, value short")
    @ResponseBody
    public HashMap<String, Object> handleAddBookRatingScore(Integer bookId, short value, @CookieValue(name = "bookShop", required = false) String bookShop ) throws NoSuchAlgorithmException {
        String errorMessage = "";
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", true);
        Integer userId = userService.getCurrentUserId(bookShop);
        BookScore bookScore = bookService.getBookScoreByBookIdAndUserId(bookId, userId);
        if (bookScore != null ) {
            bookService.modifyBookScore(bookScore.getId(), LocalDateTime.now(), value);
        } else {
            BookScore newBookScore = new BookScore();
            newBookScore.setBookId(bookId);
            newBookScore.setUserId(userId);
            newBookScore.setTime(LocalDateTime.now());
            newBookScore.setScore(value);
            bookService.newBookScore(newBookScore);
        }
        bookService.updateBookRating(bookId);
        return result;
    }

    @PostMapping("/bookReview")
    @ApiOperation("operation to add book review, parameters bookId Integer, text String")
    @ResponseBody
    public HashMap<String, Object> handleAddBookReview(Integer bookId, String text, @CookieValue(name = "bookShop", required = false) String bookShop ) throws NoSuchAlgorithmException {
        String errorMessage = "";
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", false);
        if(text.length() < 100) {
            errorMessage ="Отзыв слишком короткий. Напишите, пожалуйста, более развёрнутый отзыв";
        }

        Integer userId = userService.getCurrentUserId(bookShop);
        BookReview bookReviewSearch = bookService.getBookReviewByBookIdAndUserId(bookId, userId);
        if (bookReviewSearch !=null) {
            errorMessage ="Отзыв на данную книгу Вами уже написан.";
        }
        if(!errorMessage.isEmpty()) {
            result.put("error", errorMessage);
            return result;
        }
        result.put("result", true);
        BookReview bookReview = new BookReview();
        bookReview.setBookId(bookId);
        bookReview.setUserId(userId);
        bookReview.setTime(LocalDateTime.now());
        bookReview.setText(text);
        bookService.newBookReview(bookReview);
        return result;
    }

    @PostMapping("/rateBookReview")
    @ApiOperation("operation to add book review like/dislike, parameters reviewId Integer, value short (1/-1)")
    @ResponseBody
    public HashMap<String, Object> handleAddBookReviewLike(Integer reviewId, short value, @CookieValue(name = "bookShop", required = false) String bookShop ) throws NoSuchAlgorithmException {
        String errorMessage = "";
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", false);
        Integer userId = userService.getCurrentUserId(bookShop);
        BookReviewLike bookReviewLikeSearch = bookService.getBookReviewLikeByReviewIdAndUserId(reviewId, userId);
        if (bookReviewLikeSearch !=null) {
            errorMessage ="Оценка отзыва Вами уже сделана.";
        }
        BookReview bookReview = bookService.getBookReviewById(reviewId);
        if (bookReview == null) {
            errorMessage ="Отзыв не найден.";
        }
        if(!errorMessage.isEmpty()) {
            result.put("error", errorMessage);
            return result;
        }
        result.put("result", true);
        BookReviewLike bookReviewLike = new BookReviewLike();
        bookReviewLike.setReviewId(reviewId);
        bookReviewLike.setUserId(userId);
        bookReviewLike.setTime(LocalDateTime.now());
        bookReviewLike.setValue(value);
        bookService.newBookReviewLike(bookReviewLike);
        bookService.setReviewRating(bookReview.getRating() + value,reviewId);


        return result;
    }


    @GetMapping("/books/by-title")
    @ApiOperation("get books by title")
    public ResponseEntity<ApiResponse<Book>> booksByTitle(@RequestParam("title")String title) throws BookstoreApiWrongParameterException {
        ApiResponse<Book> response = new ApiResponse<>();
        List<Book> data = bookService.getBooksByTitle(title);
        response.setDebugMessage("successful request");
        response.setMessage("data size: "+data.size()+" elements");
        response.setStatus(HttpStatus.OK);
        response.setTimeStamp(LocalDateTime.now());
        response.setData(data);
        return ResponseEntity.ok(response);
    }



//    @GetMapping("/books/by-price-range")
//    @ApiOperation("get books by price range from min price to max price")
//    public ResponseEntity<List<Book>> priceRangeBookss(@RequestParam("min")Integer min, @RequestParam("max")Integer max){
//        return ResponseEntity.ok(bookService.getBooksWithPriceBetween(min, max));
//    }
//
//    @GetMapping("/books/with-max-discount")
//    @ApiOperation("get list of book with max price")
//    public ResponseEntity<List<Book>> maxPriceBooks(){
//        return ResponseEntity.ok(bookService.getBooksWithMaxPrice());
//    }
//
//    @GetMapping("/books/bestsellers")
//    @ApiOperation("get bestseller book (which is_bestseller = 1)")
//    public ResponseEntity<List<Book>> bestSellerBooks(){
//        return ResponseEntity.ok(bookService.getBestsellers());
//    }

//    @GetMapping("/books/by-author")
//    @ApiOperation("operation to get book list of bookshop by passed author")
//    public ResponseEntity<List<Book>> booksByAuthor(@RequestParam("author") Author author){
//        return ResponseEntity.ok(bookService.getBooksByAuthor(author));
//    }
//



}
