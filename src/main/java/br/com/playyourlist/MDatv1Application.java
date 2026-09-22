package br.com.playyourlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MDatv1Application {

	public static void main(String[] args) {
		SpringApplication.run(MDatv1Application.class, args);
	}

}
