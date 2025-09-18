package com.example.jwt_auth.auth.api;

import com.example.jwt_auth.auth.dto.*;
import com.example.jwt_auth.auth.service.AuthService;
import com.example.jwt_auth.auth.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthApi {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        // 사용자가 입력한 사용자 이름이 이미 존재하는지 확인
        if (authService.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signUp(request));
    }

    @PostMapping("/signIn")
    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        // 사용자 인증 후 액세스 토큰 및 리프레시 토큰 생성
        TokenResponse tokenResponse = authService.signIn(request);

        // 리프레시 토큰을 쿠키에 저장
        Cookie cookie = createCookie(tokenResponse.refreshToken());
        // 응답에 쿠키 추가
        response.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(TokenResponse.builder()
                        .accessToken(tokenResponse.accessToken())
                        .build());
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<TokenResponse> reissueRefreshToken(
            @RequestBody ReissueRefreshTokenRequest reissueRefreshTokenRequest,
            HttpServletResponse response
    ) {
        // 리프레시 토큰이 DB에 존재하는지 확인하고, 존재할 경우 새로운 액세스 토큰과 리프레시 토큰을 생성
        TokenResponse tokenResponse = authService.reissueRefreshToken(reissueRefreshTokenRequest);

        // 리프레시 토큰을 쿠키에 저장
        Cookie cookie = createCookie(tokenResponse.refreshToken());
        // 응답에 쿠키 추가
        response.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(TokenResponse.builder()
                        .accessToken(tokenResponse.accessToken())
                        .build());
    }

    @PostMapping("/login/oauth")
    public ResponseEntity<TokenResponse> oauthLogin(
            @RequestBody OAuthLoginRequest request,
            HttpServletResponse response
    ) {
        // OAuth 인증 후, 서버(OAuth가 아닌)에서 JWT Access/Refresh 토큰 발급
        TokenResponse tokenResponse = oAuthService.oauthLogin(request.provider(), request.authorizationCode());

        // 리프레시 토큰을 쿠키에 저장
        Cookie cookie = createCookie(tokenResponse.refreshToken());
        response.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(TokenResponse.builder()
                        .accessToken(tokenResponse.accessToken())
                        .build());
    }

    private Cookie createCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true); // HTTP Only 옵션 설정
        cookie.setSecure(true); // Secure 옵션 설정 (HTTPS 환경에서만 전송)
        cookie.setPath("/"); // 쿠키가 유효한 경로 설정
        cookie.setMaxAge(7 * 24 * 60 * 60); // 쿠키의 유효 기간 설정 (예: 7일)

        return cookie;
    }
}
