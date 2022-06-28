package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.review.BookScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookScoreRepository extends JpaRepository<BookScore, Integer> {


//    @Modifying
//    @Query(value = "insert into book_score (book_id, user_id, time, score) values (?1, ?2, ?3, ?4)", nativeQuery = true)
//    void addNewBookScore(Integer book_id, Integer user_id, LocalDateTime time, short score);

//    @Query(value = "SELECT * FROM book_score WHERE book_id = ?1 and user_id = ?2", nativeQuery = true)
//    BookScore getBookScoreByUserId(Integer book_id, Integer user_id);

    BookScore findBookScoreByBookIdAndUserId(Integer book_id, Integer user_id);

    @Modifying
    @Query(value = "UPDATE book_score SET time = ?2, score = ?3 WHERE id = ?1 ", nativeQuery = true)
    void updateBookScore(Integer scoreId, LocalDateTime time, Integer score);

    @Query(value =  "SELECT avg(score) FROM book_score where book_id = ?1 group by book_id", nativeQuery = true)
    double getBookRating(Integer bookId);

    @Query(value =  "SELECT score, count(score) FROM book_score WHERE book_id = ?1 AND score > 0 GROUP BY score ORDER BY score DESC", nativeQuery = true)
    List<Integer[]> getDataForBookRatingTable(Integer bookId);

    @Query(value =  "SELECT count(score) FROM book_score WHERE book_id = ?1 AND score > 0", nativeQuery = true)
    Integer  getCountBookScore(Integer bookId);

    @Modifying
    void deleteBookScoreByUserId(Integer userId);
}
