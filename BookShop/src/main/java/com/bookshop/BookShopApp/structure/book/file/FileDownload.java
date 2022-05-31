package com.bookshop.BookShopApp.structure.book.file;


import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

@Entity
@Table(name = "file_download")
@ApiModel(description = "entity representing a file download count")
public class FileDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("file download id generated by db automatically")
    private int id;

    @Column(name="user_id", columnDefinition = "INT NOT NULL")
    @ApiModelProperty("user id from table users")
    private int userId;

    @Column(name="book_id", columnDefinition = "INT NOT NULL")
    @ApiModelProperty("book id from table books")
    private int bookId;

    @Column(columnDefinition = "INT NOT NULL DEFAULT 1")
    @ApiModelProperty("file download count")
    private int count;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private Book fileBook;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private User fileUser;

    public Book getFileBook() {
        return fileBook;
    }

    public void setFileBook(Book fileBook) {
        this.fileBook = fileBook;
    }

    public User getFileUser() {
        return fileUser;
    }

    public void setFileUser(User fileUser) {
        this.fileUser = fileUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}