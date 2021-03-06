package com.bookshop.BookShopApp.structure.book.file;

import com.bookshop.BookShopApp.structure.book.Book;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

@Entity
@Table(name = "book_file")
@ApiModel(description = "entity representing a book file")
public class BookFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("book file id generated by db automatically")
    private int id;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    @ApiModelProperty("file hash fo identification")
    private String hash;

    @Column(name="type_id", columnDefinition = "INT NOT NULL")
    @ApiModelProperty("file type from table book_file_type")
    private int typeId;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    @ApiModelProperty("file path")
    private String path;

    @Column(name="book_id", columnDefinition = "INT NOT NULL")
    @ApiModelProperty("book id from table book")
    private int bookId;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id",  nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private Book fileBook;


    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private BookFileType fileType;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Book getFileBook() {
        return fileBook;
    }

    public void setFileBook(Book fileBook) {
        this.fileBook = fileBook;
    }

    public BookFileType getFileType() {
        return fileType;
    }

    public void setFileType(BookFileType fileType) {
        this.fileType = fileType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
