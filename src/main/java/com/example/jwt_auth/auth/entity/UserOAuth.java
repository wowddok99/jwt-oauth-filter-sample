package com.example.jwt_auth.auth.entity;

import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import com.example.jwt_auth.common.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserOAuth extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = true)
    private String oauthEmail;

    @Column
    private String nickname;

    @Column
    private String profileImageUrl;
}