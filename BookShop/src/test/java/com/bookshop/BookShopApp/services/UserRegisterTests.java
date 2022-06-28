package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.UserRepository;
import com.bookshop.BookShopApp.security.JwtRequest;
import com.bookshop.BookShopApp.security.JwtResponse;
import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.User;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDateTime;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class UserRegisterTests {

    private final UserRegister userRegister;
    private RegistrationForm registrationForm;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepositoryMock;


    @Autowired
    public UserRegisterTests(UserRegister userRegister, RegistrationForm registrationForm, UserService userService, PasswordEncoder passwordEncoder) {
        this.userRegister = userRegister;
        this.registrationForm = registrationForm;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() {
        registrationForm = new RegistrationForm();
        registrationForm.setEmail("test@mail.org");
        registrationForm.setName("Tester");
        registrationForm.setPhone("9031232323");
    }

    @AfterEach
    void tearDown() {
        registrationForm = null;
    }

    @Test
    void registerNewUser() {
        User user = userRegister.registerNewUser(registrationForm, null);
        user.setId(1);
        userService.addUserContact(user.getId(), registrationForm.getPhone(), ContactType.PHONE, "123 123", LocalDateTime.now());
        userService.addUserContact(user.getId(), registrationForm.getEmail(), ContactType.EMAIL, "123 123", LocalDateTime.now());
        assertNotNull(user);
        assertTrue(CoreMatchers.is(user.getName()).matches(registrationForm.getName()));
        Mockito.verify(userRepositoryMock, Mockito.times(1))
                .save(Mockito.any(User.class));
    }

    @Test
    void registerNewUserFail(){
        Mockito.doReturn(new User())
                .when(userRepositoryMock)
                .findBookstoreUserByContact(registrationForm.getEmail(), 1);

        User user = userRegister.registerNewUser(registrationForm, null);
        assertNull(user);
    }

    @Test
    void jwtLogin() {
        User user = userRegister.registerNewUser(registrationForm, null);
        user.setCode(passwordEncoder.encode("123 123"));
        Mockito.doReturn(user)
                .when(userRepositoryMock)
                .findBookstoreUserByContact(registrationForm.getEmail(), 1);
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setContact(registrationForm.getEmail());
        jwtRequest.setCode("123 123");
        JwtResponse jwtResponse = userRegister.jwtLogin(jwtRequest);
        assertNotNull(jwtResponse);
        assertFalse(jwtResponse.isResult());
        assertFalse(jwtResponse.getAccessToken().isEmpty());
        assertFalse(jwtResponse.getRefreshToken().isEmpty());
        assertTrue(jwtResponse.getError().isEmpty());
    }
}