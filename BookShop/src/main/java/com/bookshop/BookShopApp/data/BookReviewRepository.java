package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.review.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookReviewRepository extends JpaRepository<BookReview, Integer> {

    BookReview findBookReviewByBookIdAndUserId(Integer bookId, Integer userId);

    List<BookReview> findBookReviewByBookId(Integer bookId);

    @Query(value = "SELECT book_review.time as time, book_review.text as text, COALESCE(sel1.like, 0) as like, COALESCE(sel2.dislike,0) as dislike, users.name as name, book_review.rating as rating, book_review.id as review_id FROM book_review " +
            "LEFT JOIN (SELECT review_id, count(value) as like FROM public.book_review_like  where value = 1 group by value, review_id) sel1 ON sel1.review_id = book_review.id " +
            "LEFT JOIN (SELECT review_id, count(value) as dislike FROM public.book_review_like where value = -1 group by value, review_id) sel2 ON sel2.review_id = book_review.id " +
            "LEFT JOIN users ON book_review.user_id = users.id " +
            "WHERE book_review.book_id = ?1 ORDER BY rating DESC", nativeQuery = true)
    List<Object> getAllReviewsWithCountLikesByBookId(Integer bookId);


    @Query(value = "SELECT book_review.id as review_id, COALESCE(sel1.like, 0) as like, COALESCE(sel2.dislike,0) as dislike FROM book_review \n" +
            "LEFT JOIN (SELECT review_id, count(value) as like FROM public.book_review_like  where value = 1 group by value, review_id) sel1 ON sel1.review_id = book_review.id\n" +
            "LEFT JOIN (SELECT review_id, count(value) as dislike FROM public.book_review_like where value = -1 group by value, review_id) sel2 ON sel2.review_id = book_review.id", nativeQuery = true)
    List<Integer[]> getDataAllReviewRatings();

    @Modifying
    @Query(value = "UPDATE book_review SET rating = ?1 where id = ?2 ", nativeQuery = true)
    void setReviewRating(Integer rating, Integer reviewId);

    @Query(value = "select sum(rating) from book_review ", nativeQuery = true)
    Integer getSumAllRatings();

    BookReview findBookReviewById(Integer reviewId);

    @Modifying
    void deleteBookReviewByUserId(Integer userId);

}
