package com.example.jwt_auth.auth.repository;

import com.example.jwt_auth.auth.entity.UserOAuth;
import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOAuthRepository extends JpaRepository<UserOAuth, Long> {

    /**
     * OAuth Provider와 Provider ID로 UserOAuth 조회
     */
    Optional<UserOAuth> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}