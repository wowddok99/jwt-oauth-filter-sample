package com.example.jwt_auth.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record OAuthProperties(
        Google google,
        Kakao kakao,
        Naver naver
) {
    public record Google(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {}

    public record Kakao(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {}

    public record Naver(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {}
}