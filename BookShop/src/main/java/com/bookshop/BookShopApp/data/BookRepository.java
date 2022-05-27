package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;

public interface BookRepository extends JpaRepository<Book,Integer> {

    List<Book> findBooksByTitleContaining(String bookTitle);

    Page<Book> findBookByTitleContaining(String bookTitle, Pageable nextPage);

    @Query(value =  "SELECT * FROM book order by rating, pub_date DESC", nativeQuery = true)
    Page<Book> findOrderByRatingAndPubDate(Pageable nextPage);

    @Query(value =  "SELECT * FROM book order by popularity DESC ", nativeQuery = true)
    Page<Book> findBookOrderByPopularityDesc(Pageable nextPage);

    Page<Book> findBookByPubDateBetweenOrderByPubDateDesc(Date startDate,Date endDate,Pageable nextPage);

    @Modifying
    @Query(value = "UPDATE book SET rating = ?1 where id = ?2 ", nativeQuery = true)
    void setRatingByBookId(short rating, Integer bookId);

    @Modifying
    @Query(value = "UPDATE book SET popularity = ?1 where id = ?2 ", nativeQuery = true)
    void setPopularityByBookId(double popularity, Integer bookId);


    @Query(value = "SELECT * FROM book WHERE id in (SELECT book_id FROM book2tag WHERE tag_id = ?1)",nativeQuery = true)
    Page<Book> findAllBooksByTagId(Integer tagId,Pageable nextPage);

    @Query(value = "SELECT * FROM book WHERE id in (SELECT book_id FROM book2genre WHERE genre_id = ?1)",nativeQuery = true)
    Page<Book> findAllBooksByGenreId(Integer GenreId,Pageable nextPage);

    @Query(value = "SELECT * FROM book WHERE id in (SELECT book_id FROM book2author WHERE author_id = ?1)",nativeQuery = true)
    Page<Book> findAllBooksByAuthorId(Integer AuthorId,Pageable nextPage);

    Book findBookBySlug(String slug);

    Book findBookById(Integer id);

    List<Book> findBooksBySlugIn(String[] slugs);

    List<Book> findBooksByIdIn(Integer[] ids);

    @Query(value = "SELECT * FROM book WHERE id in (SELECT book_id FROM book2user WHERE type_id = (SELECT id FROM book2user_type WHERE code = ?2 ) and user_id = ?1 )" , nativeQuery = true)
    List<Book> findBooksByUserIdAndStatus(Integer userId, String status);




}
