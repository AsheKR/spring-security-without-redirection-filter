package com.example.demo.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthHelper {
    private final String secretKey = "c3ZQMnBrQWl3NG9EcC05eXhoV0VAcWRXWTZ3QzhEa2RYdDhhcmQ3eWV5dWVHRXJIUEBHRXZFQTJAdFlDMlZBR3FSOER4ZkZAaVBBYW1BTiFvQDdQVG5RY3ZYdUFQZnRDUWdrdwo=";
    private Key key;
    private final Integer expireTime = 86400000;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Token issueToken(String id) {
        return Token.builder()
                .token(Jwts.builder()
                        .setSubject(id)
                        .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                        .signWith(key, SignatureAlgorithm.HS256)
                        .compact())
                .build();
    }

    public Claims getTokenClaims(String accessToken) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();
            return parser.parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
