package com.bookshop.BookShopApp.structure.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

@Entity
@Table(name = "book2tag")
@ApiModel(description = "Connections books and tags")
public class Book2Tag {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @ApiModelProperty("connection id generated by db automatically")
        private int id;

        @Column(name="book_id", columnDefinition = "INT NOT NULL")
        @ApiModelProperty("id book from table book")
        private int bookId;

        @Column(name="tag_id", columnDefinition = "INT NOT NULL")
        @ApiModelProperty("tag id from table tag")
        private int tagId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}