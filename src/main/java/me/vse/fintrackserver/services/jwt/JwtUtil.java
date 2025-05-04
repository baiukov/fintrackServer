package me.vse.fintrackserver.services.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    protected String SECRET_KEY;

    @Value("${jwt.accessExpiration}")
    private long ACCESS_EXPIRATION;

    @Value("${jwt.refreshExpiration}")
    private long REFRESH_EXPIRATION;

    private Key key;

    @PostConstruct
    private void init() {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateAccessToken(String userName) {
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String userName) {
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String extractUserName(String token) {
        return Jwts.parser()
                .setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
