package com.PFE.DTT.config;

import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public WebSocketConfig(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(handshakeHandler())
                .withSockJS();
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                URI uri = request.getURI();
                String query = uri.getQuery();

                if (query != null) {
                    Optional<String> tokenParam = List.of(query.split("&")).stream()
                            .filter(p -> p.startsWith("token="))
                            .map(p -> p.substring("token=".length()))
                            .findFirst();

                    if (tokenParam.isPresent()) {
                        String token = tokenParam.get();
                        try {
                            Long userId = jwtUtil.extractClaims(token).get("id", Integer.class).longValue();
                            System.out.println("✅ WebSocket handshake - Authenticated user ID: " + userId);
                            return () -> String.valueOf(userId);
                        } catch (Exception e) {
                            System.out.println("❌ Invalid JWT in WebSocket handshake: " + e.getMessage());
                        }
                    }
                }

                System.out.println("❌ WebSocket handshake - No token in query params");
                return null;
            }
        };
    }
}
