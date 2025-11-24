package com.chat_app.channel_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChannelServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChannelServiceApplication.class, args);
	}

}
