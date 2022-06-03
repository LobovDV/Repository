package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.*;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.User;
import com.bookshop.BookShopApp.structure.user.UserContact;
import com.bookshop.BookShopApp.util.AdditionalMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final Book2UserRepository book2UserRepository;
    private final BookScoreRepository bookScoreRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewLikeRepository bookReviewLikeRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRegister userRegister;

    public UserService(com.bookshop.BookShopApp.data.UserRepository userRepository, UserContactRepository userContactRepository, Book2UserRepository book2UserRepository, BookScoreRepository bookScoreRepository,
                       BookReviewRepository bookReviewRepository, BookReviewLikeRepository bookReviewLikeRepository, MessageRepository messageRepository, PasswordEncoder passwordEncoder,
                       UserRegister userRegister) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
        this.book2UserRepository = book2UserRepository;
        this.bookScoreRepository = bookScoreRepository;
        this.bookReviewRepository = bookReviewRepository;
        this.bookReviewLikeRepository = bookReviewLikeRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRegister = userRegister;
    }

    public User getUserById(Integer id) {
        return userRepository.findUserById(id);
    }

    public Integer getUserIdFromCookie(String bookShop) {
        int userId = 0;
        if ((bookShop != null) & (bookShop !="")){
            if (userRepository.findUserById(Integer.valueOf(bookShop)) != null) {
                userId = Integer.parseInt(bookShop);
            }
        }
        return userId;
    }

    public Integer newUser (String name)  {
        User user = new User();
        user.setBalance(0);
        user.setRegTime(LocalDateTime.now());
        user.setName(name);
        user.setHash(AdditionalMethod.createMD5Hash(name + LocalDateTime.now()));
        user.setRoles("USER");
        userRepository.save(user);
        return user.getId();
    }

    public void removeAnonymousUsersMoreThanThirtyDays() {
        List<User> userList = userRepository.getAnonymousUserMoreThanThirtyDays();
        int userId = 0;
        for (User user: userList) {
           userId =  user.getId();
           book2UserRepository.deleteBook2UserByUserId(userId);
           bookScoreRepository.deleteBookScoreByUserId(userId);
           bookReviewLikeRepository.deleteBookReviewLikeByUserId(userId);
           bookReviewRepository.deleteBookReviewByUserId(userId);
           messageRepository.deleteMessageByUserId(userId);
        }
        userRepository.deleteAnonymousUserMoreThanThirtyDays();
    }

    public void addUserContact(Integer userId, String contact, ContactType type, String code, LocalDateTime codeTime) {
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContact(contact);
        userContact.setType(type);
        userContact.setApproved((short) 1);
        userContact.setCode(code);
        userContact.setCodeTime(codeTime);
        userContact.setCodeTrails(0);
        userContactRepository.save(userContact);
    }

    public void updateUserNameAndDateRegByUserId(String name, LocalDateTime req_time, Integer userId) {
        userRepository.modifyUserNameAndDateRegByUserId(name, req_time, userId);
    }

    public UserContact getUserContactByContact(String contact) {
        return userContactRepository.findUserContactByContact(contact);
    }

    public void updateUserContactCodeAndTime(String code, LocalDateTime time, Integer code_trials, Integer contactId) {
        userContactRepository.modifyUserContactCodeAndTime(code, time, code_trials, contactId);
    }

    public void updateUserLoginVerificationCode(String code, Integer userId) {
        userRepository.modifyUserLoginVerificationCode(passwordEncoder.encode(code), userId);
    }

    public void updateUserContactApproved(int approved, Integer contactId) {
        userContactRepository.modifyUserContactApproved(approved, contactId);
    }

    public void updateUserContactUserId(Integer userId, Integer tempUserId) {
        userContactRepository.modifyUserContactUserId(userId, tempUserId);
    }

    public List<UserContact> getUserContactsByUserId(Integer userId) {
        return userContactRepository.findUserContactByUserId(userId);
    }

    public Integer getCurrentUserId(String bookShop) {
        Integer userId = 0;
        if (userRegister.getAuthenticationStatus()) {
            userId = userRegister.getCurrentUser().getId();
        } else {
            userId = getUserIdFromCookie(bookShop);
        }
        return userId;
    }

    public User getBookstoreUserByContact(String contact, int type) {
        return userRepository.findBookstoreUserByContact(contact, type);
    }
}
