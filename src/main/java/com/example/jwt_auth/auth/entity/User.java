package com.example.jwt_auth.auth.entity;

import com.example.jwt_auth.auth.entity.enums.Role;
import com.example.jwt_auth.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(unique = true, nullable = true)
    private String email; // 자체 로그인 이메일

    @Column(nullable = false)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
