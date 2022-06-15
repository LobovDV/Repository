package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.author.Author;
import com.bookshop.BookShopApp.structure.book.Book;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class BookRepositoryTests {

    private final BookRepository bookRepository;

    @Autowired
    public BookRepositoryTests(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }



    @Test
    void findAllBooksByAuthorId() {
        Integer authorId = 1;
        Pageable nextPage = PageRequest.of(0,20);
        Page<Book> bookListByAuthorId = bookRepository.findAllBooksByAuthorId(authorId, nextPage);
        assertNotNull(bookListByAuthorId);
        assertFalse(bookListByAuthorId.isEmpty());
        for (Book book : bookListByAuthorId) {
            Logger.getLogger(this.getClass().getSimpleName()).info(book.toString());
            List<Author> authors = book.getAuthors();
            boolean flagAuthor = false;
            for (Author author : authors) {
              if (author.getId() == authorId) {continue;}
            }
            assertThat(flagAuthor);
        }
    }

    @Test
    void findBooksByTitleContaining() {
        String token = "Life";
        List<Book> bookListByTitleContaining = bookRepository.findBooksByTitleContaining(token);
        assertNotNull(bookListByTitleContaining);
        assertFalse(bookListByTitleContaining.isEmpty());

        for (Book book : bookListByTitleContaining) {
            Logger.getLogger(this.getClass().getSimpleName()).info(book.toString());
            assertThat(book.getTitle()).contains(token);
        }
    }

    @Test
    void findAllBooksByTagId() {
        Integer tagId = 1;
        Pageable nextPage = PageRequest.of(0,20);
        Page<Book> bookListByTagId = bookRepository.findAllBooksByTagId(tagId, nextPage);

        assertNotNull(bookListByTagId);
        assertFalse(bookListByTagId.isEmpty());

        for (Book book : bookListByTagId) {
            Logger.getLogger(this.getClass().getSimpleName()).info(book.toString());
            List<com.bookshop.BookShopApp.structure.tag.Tag> tags = book.getTags();
            boolean flagTag = false;
            for (com.bookshop.BookShopApp.structure.tag.Tag tag : tags) {
                if (tag.getId() == tagId) {continue;}
            }
            assertThat(flagTag);
        }
    }



//    @Test
//    void getBestsellers() {
//        List<Book> bestSellersBooks = bookRepository.getBestsellers();
//        assertNotNull(bestSellersBooks);
//        assertFalse(bestSellersBooks.isEmpty());
//
//        assertThat(bestSellersBooks.size()).isGreaterThan(1);
//    }

//
//    @Test
//    void findBooksByAuthor_FirstName() {
//        String token = "Jelene";
//        List<Book> bookListByAuthorFirstName = bookRepository.findBooksByAuthor_FirstName(token);
//
//        assertNotNull(bookListByAuthorFirstName);
//        assertFalse(bookListByAuthorFirstName.isEmpty());
//
//        for (Book book : bookListByAuthorFirstName) {
//            Logger.getLogger(this.getClass().getSimpleName()).info(book.toString());
//            assertThat(book.getAuthor().getFirstName()).contains(token);
//        }
//    }
}



