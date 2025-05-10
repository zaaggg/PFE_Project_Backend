package com.PFE.DTT.security;

import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        System.out.println("Processing JWT for " + requestURI + ": " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid JWT for " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(email).orElse(null);

                if (user == null) {
                    System.out.println("User not found for email: " + email);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (!jwtUtil.isTokenExpired(jwt)) {
                    List<GrantedAuthority> authorities;
                    try {
                        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    } catch (NullPointerException e) {
                        System.out.println("Invalid user role for " + email + ": " + e.getMessage());
                        authorities = Collections.emptyList();
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, null, authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authenticated user: " + email + " with role: " + authorities);
                } else {
                    System.out.println("JWT expired for " + email);
                }
            } else {
                System.out.println("Invalid email or authentication already set for " + requestURI);
            }
        } catch (Exception e) {
            System.out.println("JWT authentication error for " + requestURI + ": " + e.getMessage());
            // Optionally, set response status to 401 Unauthorized
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // return;
        }

        filterChain.doFilter(request, response);
    }
}