package com.joeun.global.util;

import com.joeun.domain.users.entity.User;
import com.joeun.global.config.LoginUser;
import com.joeun.service.user.UserDomainService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDomainService userDomainService;

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);

      try {
        Long userId = jwtUtil.parseSubjectAsLong(token);
        User user = userDomainService.findById(userId)
            .orElse(null);
        if (user != null) {
          var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
          var principal = new LoginUser(
              user.getId(),
              user.getUniversity() != null ? user.getUniversity().getId() : null,
              user.getStudentId(),
              user.getEmail(),
              user.getName(),
              authorities
          );

          var authentication = new UsernamePasswordAuthenticationToken(
              principal, null, authorities
          );
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception ignore) {
        // 토큰 문제면 인증 없이 계속 진행(권한 필요한 곳에서 401/403 처리)
      }
    }

    chain.doFilter(req, res);
  }
}