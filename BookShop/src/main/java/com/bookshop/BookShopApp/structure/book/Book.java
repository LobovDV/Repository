package com.bookshop.BookShopApp.structure.book;

import com.bookshop.BookShopApp.structure.author.Author;
import com.bookshop.BookShopApp.structure.book.file.BookFile;
import com.bookshop.BookShopApp.structure.book.file.FileDownload;
import com.bookshop.BookShopApp.structure.book.review.BookReview;
import com.bookshop.BookShopApp.structure.book.review.BookScore;
import com.bookshop.BookShopApp.structure.payments.BalanceTransaction;
import com.bookshop.BookShopApp.structure.tag.Tag;
import com.bookshop.BookShopApp.structure.genre.Genre;
import com.bookshop.BookShopApp.structure.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "book")
@ApiModel(description = "entity representing a book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id book generated by db automatically")
    private Integer id;

    @ApiModelProperty("date of book publication")
    @Column(name = "pub_date")
    private Date pubDate;

    @Column(name = "is_bestseller", columnDefinition = "SMALLINT")
    @ApiModelProperty("if isBestseller = 1 so the book is considered to be bestseller and if 0 the book is not a bestseller")
    private short isBestseller;

    @ApiModelProperty("mnemonic identity sequence of characters")
    private String slug;

    @ApiModelProperty("book title")
    private String title;

    @ApiModelProperty("book image url")
    private String image;

    @Column(columnDefinition = "text")
    @ApiModelProperty("book description text")
    private String description;

    @Column(columnDefinition = "INT NOT NULL")
    @ApiModelProperty("book price without discount")
    private Integer price;

    @Column(columnDefinition = "SMALLINT NOT NULL DEFAULT 0")
    @ApiModelProperty(value = "discount value for book in percent", example = "25")
    private short discount;

    @ApiModelProperty(value = "book rating from 0 to 5")
    private short rating;

    @ApiModelProperty(value = "book popularity")
    private double popularity;

    @ManyToMany(mappedBy = "authorBooks")
    @JsonIgnore
    private List<Author> authors;

    @ManyToMany
    @JoinTable(name = "book2genre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @JsonIgnore
    private List<Genre> genres;

    @ManyToMany(mappedBy = "userBooks")
    @JsonIgnore
    private List<User> users;

    @OneToMany(mappedBy = "reviewBook")
    @JsonIgnore
    private List<BookReview> reviews;

    @OneToMany(mappedBy = "transactionBook")
    @JsonIgnore
    private List<BalanceTransaction> transactions;

    @OneToMany(mappedBy = "fileBook")
    @JsonIgnore
    private List<FileDownload> files;

    @OneToMany(mappedBy = "scoreBook")
    @JsonIgnore
    private List<BookScore> scores;

    @ManyToMany
    @JoinTable(name = "book2tag",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @JsonIgnore
    private List<Tag> tags;

    @OneToMany(mappedBy = "fileBook")
    @JsonIgnore
    private List<BookFile> bookFilesList;


    public List<Tag> getTags() {
        return tags;
    }

    @JsonProperty
    public Integer priceWithDiscount(){
        Integer priceWithDiscountInt = price - Math.round(price*discount/100);
        return priceWithDiscountInt;
    }

    @JsonProperty
    public String getAuthorsString() {
        if (authors.size() == 0) {return "";}
        String result = authors.get(0).getName();
        if (authors.size() > 1) {result = result + " и другие";}
        return result;
    }

    @JsonIgnore
    public List<Integer> getBookRatingList() {
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i <= rating; i++ ) {
            result.add(1);
        }
        for (int i=rating + 1; i <= 5; i++ ) {
            result.add(0);
        }
        return result;
    }

    public List<BookFile> getBookFilesList() {
        return bookFilesList;
    }

    public void setBookFilesList(List<BookFile> bookFilesList) {
        this.bookFilesList = bookFilesList;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<BookScore> getScores() {
        return scores;
    }

    public void setScores(List<BookScore> scores) {
        this.scores = scores;
    }

    public List<FileDownload> getFiles() {
        return files;
    }

    public void setFiles(List<FileDownload> files) {
        this.files = files;
    }

    public List<BalanceTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<BalanceTransaction> transactions) {
        this.transactions = transactions;
    }

    public short getRating() {
        return rating;
    }

    public void setRating(short rating) {
        this.rating = rating;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public List<BookReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<BookReview> reviews) {
        this.reviews = reviews;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pub_date) {
        this.pubDate = pubDate;
    }

    public short getIsBestseller() {
        return isBestseller;
    }

    public void setIsBestseller(short is_bestseller) {
        this.isBestseller = is_bestseller;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public short getDiscount() {
        return discount;
    }

    public void setDiscount(short discount) {
        this.discount = discount;
    }
}
