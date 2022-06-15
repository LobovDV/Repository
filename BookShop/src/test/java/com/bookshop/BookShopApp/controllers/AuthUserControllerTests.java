package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.security.JwtRequest;
import com.bookshop.BookShopApp.security.RegistrationForm;
import com.bookshop.BookShopApp.services.UserRegister;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class AuthUserControllerTests {

    private final MockMvc mockMvc;
    private final UserRegister userRegister;
    private final UserService userService;

    @Autowired
    public AuthUserControllerTests(MockMvc mockMvc, UserRegister userRegister, UserService userService) {
        this.mockMvc = mockMvc;
        this.userRegister = userRegister;
        this.userService = userService;
    }

    @Test
    public void correctLoginTest() throws Exception {

        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setContact("test@test.com");
        jwtRequest.setCode("123 123");
        loginTest(jwtRequest);
        jwtRequest.setContact("+7(903)123-23-23");
        loginTest(jwtRequest);
    }

    public void loginTest(JwtRequest jwtRequest) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jwtRequest));

        mockMvc.perform(mockRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().exists("refresh"));
    }

    @Test
    public void correctLogoutTest() throws Exception {
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setContact("test@test.com");
        jwtRequest.setCode("123 123");
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jwtRequest));

        mockMvc.perform(mockRequest).andDo(print());

        MvcResult result = mockMvc.perform(get("/logout"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(SecurityMockMvcResultMatchers.unauthenticated())
                .andExpect(redirectedUrl("/signin"))
                .andReturn();
        assertTrue(result.getResponse().getCookie("token").getValue() == null);
        assertTrue(result.getResponse().getCookie("refresh").getValue() == null);
    }

    @Test
    void handleUserRegistration() throws Exception {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setEmail("testRegister@mail.org");
        registrationForm.setName("TesterRegister");
        registrationForm.setPhone("+7(903)123-33-33");
        User userForContact = userRegister.registerNewUser(registrationForm, null);
        userService.addUserContact(userForContact.getId(), registrationForm.getPhone(), ContactType.PHONE, "123 123", LocalDateTime.now());
        userService.addUserContact(userForContact.getId(), registrationForm.getEmail(), ContactType.EMAIL, "123 123", LocalDateTime.now());
        Cookie cookie = new Cookie("bookShop", String.valueOf(userForContact.getId()));

        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/reg")
                .param("name", "TesterRegister")
                .param("phone", "+7(903)123-33-33")
                .param("email", "testRegister@mail.org")
                .cookie(cookie);

        mockMvc.perform(mockRequest)
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attribute("regOk", true));

        User user1 = userService.getBookstoreUserByContact("testRegister@mail.org", 1);
        assertNotNull(user1);
        User user2 = userService.getBookstoreUserByContact("+7(903)123-33-33", 0);
        assertNotNull(user2);

        if ((user1 != null) & (user2 != null)) {
            assertEquals(user1.getId(), user2.getId());
            assertEquals("TesterRegister", user1.getName());
            assertEquals("TesterRegister", user2.getName());
        }

        userService.removeUserContactByUserId(userForContact.getId());
        if (user1 != null) {
            userService.removeUserContactByUserId(user1.getId());
            userService.removeUserById(user1.getId());
        }
        userService.removeUserById(userForContact.getId());
    }
}