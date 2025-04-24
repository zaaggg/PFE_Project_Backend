// ✅ JwtUtil.java
package com.PFE.DTT.security;

import com.PFE.DTT.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public int extractUserId(String token) {
        Claims claims = extractClaims(token);
        return Integer.parseInt(claims.get("userId").toString());
    }

    public String generateToken(User user) {
        System.out.println("JWT secret used: " + secret);

        Map<String, Object> claims = new HashMap<>();

        claims.put("email", user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("role", user.getRole().name());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("profilePhoto", user.getProfilePhoto());

        // Embed department and plant names and ids
        if (user.getDepartment() != null) {
            claims.put("department", Map.of(
                    "id", user.getDepartment().getId(),
                    "name", user.getDepartment().getName()
            ));
        }

        if (user.getPlant() != null) {
            claims.put("plant", Map.of(
                    "id", user.getPlant().getId(),
                    "name", user.getPlant().getName(),
                    "address", user.getPlant().getAddress()
            ));
        }

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
    }


    public Claims extractClaims(String token) {
        System.out.println("JWT secret used: " + secret);

        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
