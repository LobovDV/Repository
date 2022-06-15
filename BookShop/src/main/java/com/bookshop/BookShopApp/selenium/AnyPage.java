package com.bookshop.BookShopApp.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class AnyPage {
    private String url;
    private ChromeDriver driver;

    public AnyPage(String url, ChromeDriver driver) {
        this.url = url;
        this.driver = driver;
    }

    public AnyPage callPage() {
        driver.get(url);
        return this;
    }

    public AnyPage pause() throws InterruptedException {
        Thread.sleep(2000);
        return this;
    }

    public AnyPage click(String className) {
        List<WebElement> elements = driver.findElements(By.className(className));
        if(elements.size() > 0) {
            elements.get(1).click();
        }
        return this;
    }

}
