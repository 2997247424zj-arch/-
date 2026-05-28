package com.example.ecommerceback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.example.ecommerceback.**.mapper")
@SpringBootApplication
public class ECommerceBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceBackApplication.class, args);
	}

}
