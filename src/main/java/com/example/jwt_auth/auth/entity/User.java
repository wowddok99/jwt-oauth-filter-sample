package com.example.jwt_auth.auth.entity;

import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import com.example.jwt_auth.auth.entity.enums.Role;
import com.example.jwt_auth.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true) // OAuth-only 계정은 null 허용
    private String password;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(unique = true, nullable = true)
    private String oauthEmail; // OAuth 이메일 (GOOGLE, KAKAO 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OAuthProvider oauthProvider; // OAuth 제공자 (GOOGLE, KAKAO, NONE)

    @Column(unique = true, nullable = true)
    private String oauthProviderId; // OAuth 고유 ID

    @Column(nullable = false)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // USER, ADMIN 등

    /**
     * 일반 정보 업데이트 (닉네임, 프로필)
     */
    public User update(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    /**
     * OAuth 연동 정보 업데이트
     */
    public User updateOAuthInfo(String nickname, String profileImageUrl,
                                String oauthEmail, OAuthProvider provider, String providerId) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.oauthEmail = oauthEmail;
        this.oauthProvider = provider;
        this.oauthProviderId = providerId;
        return this;
    }
}
