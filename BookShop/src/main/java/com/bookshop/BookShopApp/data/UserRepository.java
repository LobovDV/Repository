package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findUserById(Integer id);

    @Query(value = "SELECT * FROM users WHERE DATE_PART('day', NOW() - reg_time) >= 30 AND name ='Anonymous' ", nativeQuery = true)
    List<User> getAnonymousUserMoreThanThirtyDays();

    @Modifying
    void deleteUserById(Integer userId);

    @Modifying
    @Query(value = "DELETE FROM users WHERE DATE_PART('day', NOW() - reg_time) >= 30 AND name ='Anonymous' ", nativeQuery = true)
    void deleteAnonymousUserMoreThanThirtyDays();

    @Query(value = "SELECT * FROM users WHERE id = (SELECT user_id FROM user_contact WHERE type = ?2 and contact = ?1) ", nativeQuery = true)
    User findBookstoreUserByContact(String contact, int type);

    @Modifying
    @Query(value = "UPDATE users SET name = ?1, reg_time = ?2 WHERE id = ?3", nativeQuery = true)
    void modifyUserNameAndDateRegByUserId(String name, LocalDateTime req_time, Integer userId);

    @Modifying
    @Query(value = "UPDATE users SET code = ?1, code_time = ?2  WHERE id = ?3 ", nativeQuery = true)
    void modifyUserLoginVerificationCode(String code, LocalDateTime code_time, Integer userId);

    @Modifying
    @Query(value = "UPDATE users SET balance = ?1 WHERE id = ?2 ", nativeQuery = true)
    void modifyUserBalance(Integer balance, Integer userId);

    @Modifying
    @Query(value = "UPDATE users SET name = ?1 WHERE id = ?2 ", nativeQuery = true)
    void modifyUserName(String name, Integer userId);
}
