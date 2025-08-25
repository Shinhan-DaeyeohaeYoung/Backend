package com.joeun.global.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record LoginUser(
    Long id,
    Long universityId,
    String studentId,
    String email,
    String name,
    Collection<? extends GrantedAuthority> authorities
) {}