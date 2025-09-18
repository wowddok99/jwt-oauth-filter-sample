package com.example.jwt_auth.auth.repository;

import com.example.jwt_auth.auth.entity.User;
import com.example.jwt_auth.auth.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthProviderAndOauthProviderId(
            OAuthProvider oauthProvider,
            String oauthProviderId
    );
    Optional<User> findByUsername(String username);
}
