package com.example.jwt_auth.auth.dto;

import com.example.jwt_auth.auth.entity.enums.OAuthProvider;

public record OAuthLoginRequest(
        OAuthProvider provider,
        String authorizationCode
) {}