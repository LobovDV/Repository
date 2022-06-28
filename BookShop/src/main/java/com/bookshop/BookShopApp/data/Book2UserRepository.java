package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.links.Book2User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Book2UserRepository extends JpaRepository<Book2User, Integer> {

    @Modifying
//    @Query(value = "DELETE FROM book2user WHERE book_id = ?1 and user_id = ?2 ", nativeQuery = true)
    void deleteBook2UserByBookIdAndUserId(Integer bookId, Integer userId);

    Book2User findBook2UserByBookIdAndUserId(Integer bookId, Integer userId);

    @Modifying
    @Query(value = "UPDATE book2user SET type_id = (SELECT id FROM book2user_type WHERE code = ?3) WHERE book_id = ?1 and user_id = ?2",nativeQuery = true)
    void updateStatusByBookIdAndUserId(Integer bookId, Integer userId, String status);

    @Query(value = "SELECT id FROM book2user_type WHERE code = ?1",nativeQuery = true)
    Integer getStatusIdByCode(String status);

    @Query(value = "SELECT * from book2user WHERE book_id = ?2 and user_id = ?1 and type_id = (SELECT id FROM book2user_type WHERE code = ?3)" ,nativeQuery = true)
    Book2User getBook2UserByUserIdAndBookIdAndStatus(Integer userId, Integer bookId, String status);

    Book2User getBook2UserByUserIdAndBookId(Integer userId, Integer bookId);

    @Query(value =  "SELECT type_id, count(book_id) FROM book2user where book_id = ?1 group by type_id order by type_id", nativeQuery = true)
    List<Integer[]> getDataForBookPopularity(Integer bookId);

    @Modifying
    void deleteBook2UserByUserId(Integer userId);

    @Modifying
    @Query(value = "UPDATE book2user SET user_id = ?2 WHERE user_id = ?1",nativeQuery = true)
    void modifyUserId(Integer userId, Integer newUserId);
}
