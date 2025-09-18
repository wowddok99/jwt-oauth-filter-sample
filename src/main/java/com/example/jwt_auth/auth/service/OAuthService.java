package com.example.jwt_auth.auth.service;

import com.example.jwt_auth.auth.client.OAuthProviderClient;
import com.example.jwt_auth.auth.dto.OAuthUserInfo;
import com.example.jwt_auth.auth.dto.TokenResponse;
import com.example.jwt_auth.auth.entity.User;
import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import com.example.jwt_auth.auth.entity.enums.Role;
import com.example.jwt_auth.auth.repository.UserRepository;
import com.example.jwt_auth.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final UserRepository userRepository;
    private final OAuthProviderClient oAuthProviderClient;
    private final JwtUtil jwtUtil;

    /**
     * OAuth Provider와 Authorization Code를 받아 소셜 로그인을 처리하고 JWT를 발급
     */
    @Transactional
    public TokenResponse oauthLogin(OAuthProvider provider, String authorizationCode) {
        String accessToken = oAuthProviderClient.getAccessToken(provider, authorizationCode)
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow(() -> new RuntimeException("Access Token을 발급받지 못했습니다."));

        OAuthUserInfo userInfo = oAuthProviderClient.getUserInfo(provider, accessToken)
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow(() -> new RuntimeException("사용자 정보를 조회하지 못했습니다."));

        User user = findOrCreateUser(provider, userInfo);

        String serviceAccessToken = jwtUtil.createAccessToken(user);
        String serviceRefreshToken = jwtUtil.createRefreshToken(user);

        return new TokenResponse(serviceAccessToken, serviceRefreshToken);
    }

    /**
     * 사용자 정보(userInfo)를 기반으로 DB에서 사용자를 찾거나, 없으면 새로 생성
     */
    private User findOrCreateUser(OAuthProvider provider, OAuthUserInfo userInfo) {
        return userRepository.findByOauthProviderAndOauthProviderId(
                provider,
                userInfo.id()
        ).orElseGet(() -> userRepository.save(
                User.builder()
                        .username(userInfo.email())
                        .oauthEmail(userInfo.email())
                        .oauthProvider(provider)
                        .oauthProviderId(userInfo.id())
                        .nickname(userInfo.nickname())
                        .profileImageUrl(userInfo.profileImageUrl())
                        .role(Role.USER)
                        .build()
        ));
    }
}
