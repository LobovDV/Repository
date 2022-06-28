package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.user.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserContactRepository extends JpaRepository<UserContact, Integer> {

    UserContact findUserContactByContact(String contact);

    List<UserContact> findUserContactByUserId(Integer userId);

    @Modifying
    @Query(value = "UPDATE user_contact SET code = ?1, code_time = ?2, code_trails = ?3 WHERE id = ?4", nativeQuery = true)
    void modifyUserContactCodeAndTime(String code, LocalDateTime time, Integer codeTrials, Integer contactId);

    @Modifying
    @Query(value = "UPDATE user_contact SET approved = ?1 WHERE id = ?2", nativeQuery = true)
    void modifyUserContactApproved(int approved, Integer contactId);

    @Modifying
    @Query(value = "UPDATE user_contact SET user_id = ?1 WHERE user_id = ?2", nativeQuery = true)
    void modifyUserContactUserId(Integer userId, Integer tempUserId);

    @Modifying
    void deleteUserContactByUserId(Integer userId);

    @Modifying
    @Query(value = "DELETE from user_contact WHERE user_id = ?1 and type = ?2", nativeQuery = true)
    void deleteUserContactByUserIdAndType(Integer userId, int type);
}
