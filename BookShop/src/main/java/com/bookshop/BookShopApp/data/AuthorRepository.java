package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.author.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Integer> {

    Author findAuthorById(Integer authorId);
}
