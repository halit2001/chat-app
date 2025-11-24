package com.chatapp.websocket_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WebsocketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketServiceApplication.class, args);
	}

}
