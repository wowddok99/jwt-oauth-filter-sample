package com.example.jwt_auth;

import com.example.jwt_auth.common.config.ImageUploadProperties;
import com.example.jwt_auth.common.config.OAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		ImageUploadProperties.class,
		OAuthProperties.class
})
public class JwtAuthApplication {
	public static void main(String[] args) {
		SpringApplication.run(JwtAuthApplication.class, args);
	}
}
