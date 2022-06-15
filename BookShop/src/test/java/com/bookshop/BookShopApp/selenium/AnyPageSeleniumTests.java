package com.bookshop.BookShopApp.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AnyPageSeleniumTests {
    private static ChromeDriver driver;

    @BeforeAll
    static void setup() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
    }

    @AfterAll
    static void tearDown() {
        driver.quit();
    }

    @Test
    public void testAllPageAccess() throws InterruptedException {

        AnyPage mainPage = new AnyPage("http://localhost:8085/", driver);
        mainPage.callPage().pause();
        assertTrue(driver.getPageSource().contains("BOOKSHOP"));
        mainPage.click("Card-picture").pause();
        assertTrue(driver.getPageSource().contains("Скачать"));
        AnyPage genres = new AnyPage("http://localhost:8085/genres", driver);
        genres.callPage().pause();
        assertTrue(driver.getPageSource().contains("Жанры"));
        genres.click("Tag").pause();
        AnyPage recent = new AnyPage("http://localhost:8085/books/recent", driver);
        recent.callPage().pause();
        assertTrue(driver.getPageSource().contains("Новинки"));
        AnyPage popular = new AnyPage("http://localhost:8085/books/popular", driver);
        popular.callPage().pause();
        assertTrue(driver.getPageSource().contains("Популярное"));
        popular.click("Card-picture").pause();
        assertTrue(driver.getPageSource().contains("Скачать"));
        AnyPage authors = new AnyPage("http://localhost:8085/authors", driver);
        authors.callPage().pause();
        assertTrue(driver.getPageSource().contains("Авторы"));
        authors.click("Author-links").pause();
        assertTrue(driver.getPageSource().contains("Биография"));

    }

}