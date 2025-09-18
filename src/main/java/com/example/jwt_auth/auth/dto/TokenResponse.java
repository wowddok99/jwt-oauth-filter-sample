package com.example.jwt_auth.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken,
        @JsonInclude(JsonInclude.Include.NON_NULL) // 해당 필드가 null일 경우 JSON 제외
        String refreshToken
) {}
