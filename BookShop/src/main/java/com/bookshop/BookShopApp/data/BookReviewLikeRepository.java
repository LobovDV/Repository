package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.review.BookReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface BookReviewLikeRepository extends JpaRepository<BookReviewLike, Integer> {

    BookReviewLike findBookReviewLikeByReviewIdAndUserId(Integer reviewId, Integer userId);

    List<BookReviewLike> findBookReviewLikeByReviewId(Integer reviewId);

    @Modifying
    void deleteBookReviewLikeByUserId(Integer userId);

    @Modifying
    void deleteBookReviewLikeByUserIdAndReviewId(Integer userId, Integer reviewId);

}
