package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.review.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Modifying
    void deleteMessageByUserId(Integer userId);
}
