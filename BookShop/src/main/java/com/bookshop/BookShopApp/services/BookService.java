package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.*;
import com.bookshop.BookShopApp.errors.BookstoreApiWrongParameterException;
import com.bookshop.BookShopApp.structure.book.file.BookFile;
import com.bookshop.BookShopApp.structure.book.review.BookReview;
import com.bookshop.BookShopApp.structure.book.review.BookReviewLike;
import com.bookshop.BookShopApp.structure.book.review.BookScore;
import com.bookshop.BookShopApp.structure.book.Book;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class BookService {

    public class Pair {

        private int[] starList;
        private Integer count;

        public Pair(int[] starList, Integer count) {
            this.starList = starList;
            this.count = count;
        }

        public int[] getStarList() {
            return starList;
        }

        public Integer getCount() {
            return count;
        }
    }

    private final BookRepository bookRepository;
    private final BookFileRepository bookFileRepository;
    private final Book2UserRepository book2UserRepository;
    private final BookScoreRepository bookScoreRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewLikeRepository bookReviewLikeRepository;


    @Autowired
    public BookService(BookRepository bookRepository, BookFileRepository bookFileRepository, Book2UserRepository book2UserRepository, BookScoreRepository bookScoreRepository, BookReviewRepository bookReviewRepository, BookReviewLikeRepository bookReviewLikeRepository) {
        this.bookRepository = bookRepository;
        this.bookFileRepository = bookFileRepository;
        this.book2UserRepository = book2UserRepository;
        this.bookScoreRepository = bookScoreRepository;
        this.bookReviewRepository = bookReviewRepository;
        this.bookReviewLikeRepository = bookReviewLikeRepository;
    }

    public List<Book> getBooksByTitle(String title) throws BookstoreApiWrongParameterException {
        if(title.equals("") || title.length()<=1){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        }else{
            List<Book> data = bookRepository.findBooksByTitleContaining(title);
            if(data.size()>0){
                return data;
            }else {
                throw new BookstoreApiWrongParameterException("No data found with specified parameters...");
            }
        }
    }

    public Page<Book> getPageOfRecommendedBooks(Integer offset, Integer limit){
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findOrderByRatingAndPubDate(nextPage);
    }

    public Page<Book> getPageOfSearchResultBooks(String searchWord, Integer offset, Integer limit){
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findBookByTitleContaining(searchWord,nextPage);
    }

    public Page<Book> getPageOfRecentBooks(Date startDate,Date endDate, Integer offset, Integer limit){
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findBookByPubDateBetweenOrderByPubDateDesc(startDate, endDate, nextPage);
    }

    public Page<Book> getPageOfPopularBooks(Integer offset, Integer limit){
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findBookOrderByPopularityDesc(nextPage);
    }

    public Page<Book> getPageOfBooksByTagId(Integer tagId, Integer offset, Integer limit){
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findAllBooksByTagId(tagId, nextPage);
    }

    public Page<Book> getPageOfBooksByGenreId(Integer genreId, Integer offset, Integer limit)  {
            Pageable nextPage = PageRequest.of(offset, limit);
            return bookRepository.findAllBooksByGenreId(genreId, nextPage);
    }

    public Page<Book> getPageOfBooksByAuthorId(Integer authorId, Integer offset, Integer limit){
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findAllBooksByAuthorId(authorId, nextPage);
    }

    public Book getBookBySlug(String slug) {
        return bookRepository.findBookBySlug(slug);
    }

    public Book getBookById(Integer id) {
        return bookRepository.findBookById(id);
    }

    public void updateBook(Book bookToUpdate) {
        bookRepository.save(bookToUpdate);
    }

    public Book getBookByFileHash(String hash) {
        BookFile bookFile = bookFileRepository.findBookFileByHash(hash);
        return bookRepository.findBookById(bookFile.getBookId());
    }

    public List<Book> getBooksBySlugIn(String cartContents, String status) {
        if (cartContents != null) {
            cartContents = cartContents.startsWith("/") ? cartContents.substring(1) : cartContents;
            cartContents = cartContents.endsWith("/") ? cartContents.substring(0, cartContents.length() - 1) :
                    cartContents;
            String[] cookieSlugs = cartContents.split("/");
            String[] cookieSlugsStatus= Arrays.stream(cookieSlugs).filter(s->s.contains(status)).map(s->s.replace(":"+status,"")).toArray(String[]::new);
            return bookRepository.findBooksBySlugIn(cookieSlugsStatus);
        } else {
            return new ArrayList<>();
        }
    }

    public List<Book> getBooksByIdIn(String cartContents, String status) {
        if (cartContents != null) {
            cartContents = cartContents.startsWith("/") ? cartContents.substring(1) : cartContents;
            cartContents = cartContents.endsWith("/") ? cartContents.substring(0, cartContents.length() - 1) :
                    cartContents;
            String[] cookieIds = cartContents.split("/");
            Integer[] cookieIdStatus= Arrays.stream(cookieIds).filter(s->s.contains(status)).map(s->Integer.valueOf(s.replace(":"+status,""))).toArray(Integer[]::new);
            return bookRepository.findBooksByIdIn(cookieIdStatus);
        } else {
            return new ArrayList<>();
        }
    }

    public List<Book> getBooksByUserIdAndStatus (Integer userId, String status) {
        return bookRepository.findBooksByUserIdAndStatus(userId, status);
    }

    @ApiOperation("Calculate and update book rating by book id, parameter bookId - Integer")
    public short updateBookRating(Integer bookId) {
        short tempRating = (short) bookScoreRepository.getBookRating(bookId);
        bookRepository.setRatingByBookId(tempRating, bookId);
        return tempRating;
    }

    @ApiOperation("Calculate and update book popularity by book id, parameter bookId - Integer")
    public double updateBookPopularity(Integer bookId) {
        if (bookRepository.findBookById(bookId) != null) {
            List<Integer[]> dataForBookPopularity = book2UserRepository.getDataForBookPopularity(bookId);
            Integer b = 0;
            Integer c = 0;
            Integer k = 0;
            for (Integer[] item : dataForBookPopularity) {
                switch (item[0]) {
                    case (1):
                        k = item[1];
                        break;
                    case (2):
                        c = item[1];
                        break;
                    case (3):
                        b = item[1];
                        break;
                }
            }
            double popularity = b + 0.7 * c + 0.4 * k;
            bookRepository.setPopularityByBookId(popularity, bookId);
            return popularity;
        } else { return 0;}
    }

    @ApiOperation("Prepare book rating table by book id, parameter bookId - Integer")
    public List<Pair> getBookRatingTable(Integer bookId) {
        if (bookRepository.findBookById(bookId) != null) {
            List<Integer[]> dataForBookRating = bookScoreRepository.getDataForBookRatingTable(bookId);
            List<Pair> ratingTable = new ArrayList<>();
            int [][] ratingMatrix = {{1,0,0,0,0}, {1,1,0,0,0}, {1,1,1,0,0}, {1,1,1,1,0}, {1,1,1,1,1}};
            int prevRating = 5;
            for (Integer[] item : dataForBookRating) {
                while (item[0] < prevRating) {
                    ratingTable.add(new Pair(ratingMatrix[prevRating - 1], 0));
                    prevRating--;
                }
                ratingTable.add(new Pair(ratingMatrix[prevRating - 1], item[1]));
                prevRating--;
            }
            while (prevRating > 0) {
                ratingTable.add(new Pair(ratingMatrix[prevRating - 1], 0));
                prevRating--;
            }
            return ratingTable;
        }
        return new ArrayList<>();
    }

    public BookScore getBookScoreByBookIdAndUserId(Integer book_id, Integer user_id) {
        return bookScoreRepository.findBookScoreByBookIdAndUserId(book_id, user_id);
    }

    public void modifyBookScore(Integer scoreId, LocalDateTime time, short score) {
        bookScoreRepository.updateBookScore(scoreId, time, score);
    }

    public void newBookScore(BookScore bookScore) {
        bookScoreRepository.save(bookScore);
    }

    public Integer getCountBookScore(Integer bookId) {
        return bookScoreRepository.getCountBookScore(bookId);
    }

    public List<Object> getAllReviewsWithCountLikesByBookId(Integer bookId) {
        return bookReviewRepository.getAllReviewsWithCountLikesByBookId(bookId);
    }

    public BookReview getBookReviewByBookIdAndUserId(Integer bookId, Integer userId) {
        return bookReviewRepository.findBookReviewByBookIdAndUserId(bookId, userId);
    }

    public void newBookReview(BookReview bookReview) {
        bookReviewRepository.save(bookReview);
    }

    public void setAllReviewRatings() {
        if (getSumAllReviewsRatings() == 0) {
            List<Integer[]> reviewsArray = bookReviewRepository.getDataAllReviewRatings();
            for (Integer[] item : reviewsArray) {
                bookReviewRepository.setReviewRating(item[1] - item[2], item[0]);
            }
        }
    }

    public void setReviewRating(Integer rating, Integer reviewId) {
        if (bookReviewRepository.findBookReviewById(reviewId) != null) {
          bookReviewRepository.setReviewRating(rating, reviewId);
        }
    }

    public BookReviewLike getBookReviewLikeByReviewIdAndUserId(Integer reviewId, Integer userId) {
        return bookReviewLikeRepository.findBookReviewLikeByReviewIdAndUserId(reviewId, userId);
    }

    public void newBookReviewLike(BookReviewLike bookReviewLike) {
        bookReviewLikeRepository.save(bookReviewLike);
    }

    public BookReview getBookReviewById(Integer reviewId) {
        return bookReviewRepository.findBookReviewById(reviewId);
    }

    public  Integer getSumAllReviewsRatings() {
        Integer sum = bookReviewRepository.getSumAllRatings();
        if (sum != null) {
            return sum;
        } else { return 0;}
    }
}
