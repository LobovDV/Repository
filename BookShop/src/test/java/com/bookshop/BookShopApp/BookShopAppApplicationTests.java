package com.bookshop.BookShopApp;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BookShopAppApplicationTests {

	@Value("${jwt.secret.access}")
	String authSecret;

	private final BookShopAppApplication application;

	@Autowired
	public BookShopAppApplicationTests(BookShopAppApplication application) {
		this.application = application;
	}

	@Test
	void contextLoads() {
		assertNotNull(application);
	}

	@Test
	void verifyAuthSecret() {
		assertThat(authSecret, Matchers.containsString("box"));
	}


}
