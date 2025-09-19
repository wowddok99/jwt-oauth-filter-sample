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
            Properties properties,
            KakaoAccount kakao_account
    ) {
        public record Properties(
                String nickname,
                String profile_image,
                String thumbnail_image
        ) {}

        public record KakaoAccount(
                Profile profile,
                String email
        ) {
            public record Profile(
                    String nickname,
                    String profile_image_url,
                    String thumbnail_image_url
            ) {}
        }

        public OAuthUserInfo toOAuthUserInfo() {
            return new OAuthUserInfo(
                    String.valueOf(id),
                    kakao_account.email,
                    kakao_account.profile.nickname,
                    kakao_account.profile.profile_image_url
            );
        }
    }

    public record NaverUserResponse(
            String resultcode,
            String message,
            Response response
    ) {
        public record Response(
                String id,
                String nickname,
                String profile_image,
                String email
        ) {}

        public OAuthUserInfo toOAuthUserInfo() {
            return new OAuthUserInfo(
                    response.id(),
                    response.email(),
                    response.nickname(),
                    response.profile_image()
            );
        }
    }

}
