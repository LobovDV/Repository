package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.enums.TagType;
import com.bookshop.BookShopApp.structure.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository  extends JpaRepository<Tag, Integer> {

    @Query(value =  "SELECT count(book_id) FROM book2tag WHERE tag_id = ?1 GROUP BY tag_id", nativeQuery = true)
    Integer getTagBooksCount(Integer tagId);

    @Query(value =  "SELECT tag_id, count(book_id) FROM book2tag GROUP BY tag_id ORDER BY tag_id", nativeQuery = true)
    List<Integer[]> getDataAllTagBooksCount();

    @Modifying
    @Query(value = "UPDATE tag SET count_book = ?1, type = ?2 WHERE id = ?3 ", nativeQuery = true)
    void setTagBooksCount(Integer countBook, TagType type, Integer tagId);

    @Query(value = "SELECT SUM(count_book) from tag", nativeQuery = true)
    Integer getSumAllTagsCountBook();

    Tag findTagEntityById(Integer tagId);
}
