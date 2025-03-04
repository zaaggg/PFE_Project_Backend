package com.PFE.DTT.security;

import com.PFE.DTT.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public int extractUserId(String token) {
        Claims claims = extractClaims(token);
        if (claims.get("userId") == null) {
            throw new RuntimeException("User ID not found in JWT");
        }
        return Integer.parseInt(claims.get("userId").toString());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())  // ✅ Ensure userId is included
                .claim("role", user.getRole().name())  // ✅ Includes role in JWT
                .setIssuedAt(new Date())  // ✅ Token issued time
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())  // ✅ Always use secret.getBytes()
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())  // ✅ Ensure getBytes() is used here
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);  // ✅ Extracts role safely
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
