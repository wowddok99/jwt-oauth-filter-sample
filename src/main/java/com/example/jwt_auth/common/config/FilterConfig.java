package com.example.jwt_auth.common.config;

import com.example.jwt_auth.auth.entity.enums.Role;
import com.example.jwt_auth.common.jwt.JwtAuthenticationFilter;
import com.example.jwt_auth.common.jwt.JwtAuthorizationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class FilterConfig {

    /**
     * JWT 인증 필터 등록
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> authenticationFilter(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);

        // 인증 적용 URL
        registrationBean.addUrlPatterns(
                "/images", "/images/*",
                "/post", "/post/**"
        );

        // 필터 체인 실행 순서 설정
        registrationBean.setOrder(1);

        return registrationBean;
    }

    /**
     * 역할별 인가 규칙 정의
     * - JwtAuthorizationFilter에서 이 Bean을 주입받아 사용
     */
    @Bean
    public Map<String, String> roleRules() {
        Map<String, String> rules = new HashMap<>();

        // USER 권한이 필요한 URL 패턴 정의
        List<String> userPatterns = List.of(
                "/post", "/post/**"
        );

        userPatterns.forEach(pattern -> rules.put(pattern, Role.USER.name()));

        return rules;
    }

    /**
     * JWT 인가(권한 체크) 필터 등록
     */
    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> authorizationFilter(
            JwtAuthorizationFilter jwtAuthorizationFilter,
            Map<String, String> roleRules
    ) {
        FilterRegistrationBean<JwtAuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthorizationFilter);

        // roleRules의 key(URL 패턴)를 추출하여 해당 URL 요청에만 필터가 적용되도록 설정
        String[] urlPatterns = roleRules.keySet().toArray(new String[0]);
        registrationBean.addUrlPatterns(urlPatterns);

        // 필터 체인 실행 순서 설정
        registrationBean.setOrder(2);

        return registrationBean;
    }
}
