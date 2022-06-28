package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.structure.book.links.Book2User;
import com.bookshop.BookShopApp.data.Book2UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class Book2UserService {

    private final Book2UserRepository book2UserRepository;

    @Autowired
    public Book2UserService(Book2UserRepository book2UserRepository) {
        this.book2UserRepository = book2UserRepository;
    }

    public boolean deleteByBookIdAndUserId(Integer bookId, Integer userId) {
        if (book2UserRepository.findBook2UserByBookIdAndUserId(bookId, userId) != null) {
            book2UserRepository.deleteBook2UserByBookIdAndUserId(bookId, userId);
            return true;
        }
        return false;
    }

    public void updateByBookIdAnfUserId(Integer bookId, Integer userId, String status) {
        Book2User book2UserExists = book2UserRepository.findBook2UserByBookIdAndUserId(bookId, userId);
        if (book2UserExists != null)  {
            if ((book2UserExists.getTypeId() != 3) & (book2UserExists.getTypeId() != 4)){
                book2UserRepository.updateStatusByBookIdAndUserId(bookId, userId, status);
            }
        } else {
            Book2User book2User = new Book2User();
            book2User.setBookId(bookId);
            book2User.setUserId(userId);
            book2User.setTime(LocalDateTime.now());
            book2User.setTypeId(book2UserRepository.getStatusIdByCode(status));
            book2UserRepository.save(book2User);
        }
    }

    public Book2User getBook2UserByUserIdBookIdAndStatus(Integer userId, Integer bookId, String status) {
        return book2UserRepository.getBook2UserByUserIdAndBookIdAndStatus(userId, bookId, status);
    }

    public Book2User getBook2UserByUserIdAndBookId(Integer userId, Integer bookId) {
        return book2UserRepository.getBook2UserByUserIdAndBookId(userId, bookId);
    }
}
