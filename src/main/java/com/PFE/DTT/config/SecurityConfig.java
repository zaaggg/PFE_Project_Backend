package com.PFE.DTT.config;
import com.PFE.DTT.security.JwtAuthenticationFilter;
import com.PFE.DTT.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/ws-chat/**",    // WebSocket handshake endpoint
                                "/ws/**",         // SockJS fallback endpoints
                                "/error"          // Fallback for failed WebSocket handshakes
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/admin-users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin-plants/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin-departments/**").hasRole("ADMIN")



                        // or your desired role
                        // Uncomment and configure these as needed for fine-grained access control
                        /*
                        .requestMatchers("/api/rapports/create").hasAuthority("DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/my-created").hasAuthority("DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/assigned").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/maintenance-form/update/{reportId}").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/maintenance-form/{reportId}").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/specific-checklist/{reportId}").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/standard-checklist/{reportId}").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/entry/standard/{entryId}").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        .requestMatchers("/api/rapports/entry/specific/{entryId}").hasAnyAuthority("EMPLOYEE", "DEPARTMENT_MANAGER")
                        */
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}