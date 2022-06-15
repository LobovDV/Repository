package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.user.User;
import com.bookshop.BookShopApp.util.AdditionalMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class UserRepositoryTests {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryTests(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    public void testAddNewUser(){
        User user = new User();
        user.setCode("123 123");
        user.setName("Tester");
        user.setBalance(0);
        user.setRegTime(LocalDateTime.now());
        user.setHash(AdditionalMethod.createMD5Hash("Tester" + LocalDateTime.now()));
        user.setRoles("USER");

        assertNotNull(userRepository.save(user));
    }
}