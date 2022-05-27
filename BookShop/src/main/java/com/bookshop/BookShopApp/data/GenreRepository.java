package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.genre.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre,Integer> {

    @Query(value =  "SELECT count(book_id) FROM book2genre where genre_id = ?1 group by genre_id", nativeQuery = true)
    Integer getGenreBooksCount(Integer genreId);

    @Query(value =  "SELECT genre_id, count(book_id) FROM book2genre group by genre_id order by genre_id", nativeQuery = true)
    List<Integer[]> getDataAllGenreBooksCount();

    @Modifying
    @Query(value = "UPDATE genre SET count_book = ?1 where id = ?2", nativeQuery = true)
    void setGenreBooksCount(Integer countBook, Integer genreId);

    @Query(value = "SELECT SUM(count_book) FROM genre", nativeQuery = true)
    Integer getSumAllGenreBooksCount();


    Genre findGenreEntityById(Integer genreId);

    List<Genre> findGenreEntityByParentIdOrderByName(Integer parentId);
}
