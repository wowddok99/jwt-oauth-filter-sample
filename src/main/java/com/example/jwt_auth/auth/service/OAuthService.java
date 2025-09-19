package com.example.jwt_auth.auth.service;

import com.example.jwt_auth.auth.client.OAuthProviderClient;
import com.example.jwt_auth.auth.dto.OAuthUserInfo;
import com.example.jwt_auth.auth.dto.TokenResponse;
import com.example.jwt_auth.auth.entity.User;
import com.example.jwt_auth.auth.entity.UserOAuth;
import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import com.example.jwt_auth.auth.entity.enums.Role;
import com.example.jwt_auth.auth.repository.UserOAuthRepository;
import com.example.jwt_auth.auth.repository.UserRepository;
import com.example.jwt_auth.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final UserOAuthRepository userOAuthRepository;
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
     * 기존 자체 회원(네이티브 계정)이 있으면 자동으로 OAuth 계정과 연동
     */
    private User findOrCreateUser(OAuthProvider provider, OAuthUserInfo userInfo) {
        // OAuth 이메일 null 체크
        Objects.requireNonNull(userInfo.email(), "OAuth 이메일이 존재하지 않아 가입/연동할 수 없습니다.");

        // OAuth 계정 조회
        return userOAuthRepository.findByProviderAndProviderId(provider, userInfo.id())
                .map(oauth ->
                    // OAuth 계정이 이미 존재하면 해당 네이티브 계정(User) 조회 후 반환
                    userRepository.findById(oauth.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found for OAuth account"))
                )
                // OAuth 계정이 없으면 새로운 User 혹은 기존 네이티브 계정과 연동
                .orElseGet(() -> {
                    // 이메일 기준 기존 네이티브 계정 확인
                    User user = userRepository.findByEmail(userInfo.email())
                            .orElseGet(() -> {
                                // 네이티브 계정도 없으면 새 User 생성
                                return userRepository.save(User.builder()
                                        .username(userInfo.email())
                                        .nickname(userInfo.nickname())
                                        .profileImageUrl(userInfo.profileImageUrl())
                                        .role(Role.USER)
                                        .build());
                            });

                    // OAuth 계정 연동
                    userOAuthRepository.save(UserOAuth.builder()
                            .userId(user.getId())          // 네이티브 계정 ID
                            .provider(provider)            // OAuth 제공자 (GOOGLE, KAKAO 등)
                            .providerId(userInfo.id())     // OAuth 제공자 고유 ID
                            .oauthEmail(userInfo.email())  // OAuth 이메일
                            .nickname(userInfo.nickname())
                            .profileImageUrl(userInfo.profileImageUrl())
                            .build());

                    // 최종적으로 User 반환
                    return user;
                });
    }
}