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
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements Filter {

    /** URL 패턴 매칭을 위한 유틸리티 */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * URL 패턴별 접근에 필요한 최소 역할을 정의한 맵. (Key: URL 패턴, Value: 역할 이름)
     * 이 규칙 맵은 FilterConfig 클래스에서 @Bean으로 생성되어 생성자를 통해 주입됩니다.
     */
    private final Map<String, String> roleRules;

    /**
     * 인증 필터를 통과한 요청에 대해, 사용자의 역할(Role)에 기반하여 접근 권한을 검사(인가)합니다.
     */
    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String requestURI = httpRequest.getRequestURI();

        // 인증 필터에서 저장한 사용자 역할(attribute "role") 가져오기
        String userRole = (String) httpRequest.getAttribute("role");

        // 요청 URL과 매칭되는 접근 제어 규칙 확인
        for (Map.Entry<String, String> entry : roleRules.entrySet()) {
            String pattern = entry.getKey();
            String requiredRole = entry.getValue();

            if (pathMatcher.match(pattern, requestURI)) {
                // 권한이 없으면 403 Forbidden 반환
                if (userRole == null || !hasPermission(userRole, requiredRole)) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return; // 필터 체인 중단
                }
                // 권한이 충분하면 규칙 검사 종료 후 요청 계속 진행
                break;
            }
        }

        // 다음 필터 또는 컨트롤러로 요청 전달
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 사용자 역할이 필요한 역할을 만족하는지 확인
     *
     * @param userRole     사용자 역할
     * @param requiredRole 요청 URL에 필요한 역할
     * @return 권한이 있으면 true, 없으면 false
     */
    private boolean hasPermission(String userRole, String requiredRole) {
        return userRole.equals(requiredRole);
    }
}
