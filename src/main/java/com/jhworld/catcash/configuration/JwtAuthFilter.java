package com.jhworld.catcash.configuration;

import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(final JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("토큰 검사 시작");
        // 요청 헤더에서 JWT 토큰 추출
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userSeq = null;

        System.out.println(authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            userSeq = jwtUtil.extractClaims(token).getSubject();
        }

        System.out.println("형식 확인");

        System.out.println(token);

        // 토큰이 존재하고 사용자 인증이 되어 있지 않은 경우
        if (userSeq != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userEntity = userRepository.findByUserSequence(userSeq).orElse(null);

            // 토큰이 유효한 경우 인증 설정
            if (userEntity != null && jwtUtil.isTokenValid(token, userEntity.getUserSequence())) {
                System.out.println("유효");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userSeq, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));


                System.out.println("모두 통과");
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 필터 체인을 타고 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
