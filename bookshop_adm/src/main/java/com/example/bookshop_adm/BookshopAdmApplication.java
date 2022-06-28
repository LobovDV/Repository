package com.example.bookshop_adm;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class BookshopAdmApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookshopAdmApplication.class, args);
	}

}
