package com.PFE.DTT.service;

import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // ✅ Use user ID as the principal name so Spring routes WebSocket messages correctly
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),  // ✅ use ID here
                user.getPassword(),
                Collections.emptyList()
        );

    }
}
