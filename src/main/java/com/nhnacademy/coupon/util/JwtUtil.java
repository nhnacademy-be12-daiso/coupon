package com.nhnacademy.coupon.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserId(String token){
        try{
            // 1. 토큰 검증 및 파싱
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey) // secretKey로 검증
                    .build()
                    .parseSignedClaims(token) // token 검사 수행(유효시간 체크, 위조 체크)
                    .getPayload();

            // 2. 토큰에 담긴 "userId" 값을 꺼냄 (Long 타입으로 변환)
            // Auth 서버가 토큰 만들 때 넣은 키 이름이 "userId"여야 함.
            return claims.get("userId", Long.class);
        } catch (Exception e){
            throw new IllegalStateException("유효하지 않는 토큰: " + e.getMessage());
        }
    }
}
