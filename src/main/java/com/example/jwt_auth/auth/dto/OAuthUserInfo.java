package com.example.jwt_auth.auth.dto;

public record OAuthUserInfo(
        String id,
        String email,
        String nickname,
        String profileImageUrl
) {
    public record GoogleUserResponse(
            String id,
            String email,
            String name,
            String picture
    ) {
        public OAuthUserInfo toOAuthUserInfo() {
            return new OAuthUserInfo(id, email, name, picture);
        }
    }

    public record KakaoUserResponse(
            Long id,
            String email,
            String nickname,
            String profileImageUrl
    ) {
        public OAuthUserInfo toOAuthUserInfo() {
            return new OAuthUserInfo(
                    String.valueOf(id),
                    email,
                    nickname,
                    profileImageUrl
            );
        }
    }

    public record NaverUserResponse(
            String id,
            String email,
            String nickname,
            String profileImageUrl
    ) {
        public OAuthUserInfo toOAuthUserInfo() {
            return new OAuthUserInfo(
                    id,
                    email,
                    nickname,
                    profileImageUrl
            );
        }
    }
}
