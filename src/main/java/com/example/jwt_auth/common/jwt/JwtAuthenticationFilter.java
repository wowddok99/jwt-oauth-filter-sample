package com.example.jwt_auth.common.jwt;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;

    /**
     * 서블릿 필터 체인을 통해 들어오는 요청을 처리하여 JWT 기반 인증을 수행합니다.
     */
    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // 요청 헤더에서 JWT 추출
        String token = resolveToken(httpRequest);

        // 토큰이 없는 경우 401 Unauthorized 반환
        if (!StringUtils.hasText(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰 유효성 검증
        if (jwtUtil.isTokenValid(token)) {
            // 유효한 경우, 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            // 추출한 사용자 정보를 request attribute에 저장
            httpRequest.setAttribute("username", username);
            httpRequest.setAttribute("role", role);

            // 다음 필터 또는 컨트롤러로 요청 전달
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // 토큰이 유효하지 않은 경우 401 Unauthorized 반환
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * HTTP 요청 헤더에서 JWT 추출
     *
     * @param request HTTP 요청
     * @return JWT 문자열, 존재하지 않으면 null 반환
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 접두사 제거
        }
        return null;
    }
}
