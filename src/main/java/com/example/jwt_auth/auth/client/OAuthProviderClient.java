package com.example.jwt_auth.auth.client;

import com.example.jwt_auth.auth.dto.OAuthUserInfo;
import com.example.jwt_auth.auth.dto.OAuthUserInfo.GoogleUserResponse;
import com.example.jwt_auth.auth.dto.OAuthUserInfo.KakaoUserResponse;
import com.example.jwt_auth.auth.dto.OAuthUserInfo.NaverUserResponse;
import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import com.example.jwt_auth.common.config.OAuthProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuthProviderClient {

    private final WebClient webClient;
    private final OAuthProperties oAuthProperties;

    public OAuthProviderClient(WebClient.Builder webClientBuilder, OAuthProperties oAuthProperties) {
        this.webClient = webClientBuilder.build();
        this.oAuthProperties = oAuthProperties;
    }

    /**
     * Provider와 Authorization Code를 받아 Access Token 발급을 요청합니다.
     */
    public Mono<String> getAccessToken(OAuthProvider provider, String authorizationCode) {
        return switch (provider) {
            case GOOGLE -> fetchGoogleAccessToken(authorizationCode);
            case KAKAO -> fetchKakaoAccessToken(authorizationCode);
            case NAVER -> fetchNaverAccessToken(authorizationCode);
            default -> Mono.error(new IllegalArgumentException("지원하지 않는 Provider 입니다: " + provider));
        };
    }

    /**
     * Google OAuth Access Token 발급을 요청
     */
    private Mono<String> fetchGoogleAccessToken(String code) {
        // Authorization Code 디코딩 처리
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        OAuthProperties.Google google = oAuthProperties.google();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "authorization_code");
        formData.add("client_id", google.clientId());
        formData.add("client_secret", google.clientSecret());
        formData.add("redirect_uri", google.redirectUri());
        formData.add("code", decodedCode);

        return webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> (String) response.get("access_token"));
    }

    /**
     * Kakao OAuth Access Token 발급을 요청
     */
    private Mono<String> fetchKakaoAccessToken(String authorizationCode) {
        OAuthProperties.Kakao kakao = oAuthProperties.kakao();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakao.clientId());
        formData.add("redirect_uri", kakao.redirectUri());
        formData.add("code", authorizationCode);

        if (kakao.clientSecret() != null && !kakao.clientSecret().isBlank()) {
            formData.add("client_secret", kakao.clientSecret());
        }

        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> (String) response.get("access_token"));
    }

    /**
     * Naver OAuth Access Token 발급을 요청
     */
    private Mono<String> fetchNaverAccessToken(String authorizationCode) {
        OAuthProperties.Naver naver = oAuthProperties.naver();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "authorization_code");
        formData.add("client_id", naver.clientId());
        formData.add("client_secret", naver.clientSecret());
        formData.add("code", authorizationCode);

        return webClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> (String) response.get("access_token"));
    }

    /**
     * OAuth Provider에 따라 사용자 정보 조회
     */
    public Mono<OAuthUserInfo> getUserInfo(OAuthProvider provider, String oauthAccessToken) {
        return switch (provider) {
            case GOOGLE -> fetchGoogleUserInfo(oauthAccessToken);
            case KAKAO -> fetchKakaoUserInfo(oauthAccessToken);
            case NAVER -> fetchNaverUserInfo(oauthAccessToken);
            default -> Mono.error(new IllegalArgumentException("지원하지 않는 Provider 입니다: " + provider));
        };
    }

    /**
     * Google OAuth 사용자 정보 조회
     */
    private Mono<OAuthUserInfo> fetchGoogleUserInfo(String oauthAccessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(h -> h.setBearerAuth(oauthAccessToken))
                .retrieve()
                .bodyToMono(GoogleUserResponse.class)
                .map(GoogleUserResponse::toOAuthUserInfo)
                .onErrorMap(error -> new RuntimeException("Invalid Google OAuth token", error));
    }

    /**
     * Kakao OAuth 사용자 정보 조회
     */
    private Mono<OAuthUserInfo> fetchKakaoUserInfo(String oauthAccessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .headers(h -> h.setBearerAuth(oauthAccessToken))
                .retrieve()
                .bodyToMono(KakaoUserResponse.class)
                .map(KakaoUserResponse::toOAuthUserInfo)
                .onErrorMap(error -> new RuntimeException("Invalid Kakao OAuth token", error));
    }

    /**
     * Naver OAuth 사용자 정보 조회
     */
    private Mono<OAuthUserInfo> fetchNaverUserInfo(String oauthAccessToken) {
        return webClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .headers(h -> h.setBearerAuth(oauthAccessToken))
                .retrieve()
                .bodyToMono(NaverUserResponse.class)
                .map(NaverUserResponse::toOAuthUserInfo)
                .onErrorMap(error -> new RuntimeException("Invalid Naver OAuth token", error));
    }
}
