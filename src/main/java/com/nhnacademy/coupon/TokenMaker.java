package com.nhnacademy.coupon;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TokenMaker {
    public static void main(String[] args) {
        String secret = "v3s5v8y/B?E(H+MbQeThWmZq4t7w9z$C&F)J@NcRfUjXn2r5u8x/A?D(G+KaPdSg";

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .claim("userId", 12345L) // 우리가 테스트할 유저 ID (12345)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간 유효
                .signWith(key)
                .compact();

        System.out.println("⬇️ 아래 토큰을 복사해서 Postman에 넣으세요 ⬇️");
        System.out.println("Bearer " + token);
    }
}
