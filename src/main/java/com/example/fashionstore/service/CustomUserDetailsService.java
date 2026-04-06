package com.example.fashionstore.service;

import com.example.fashionstore.model.User;
import com.example.fashionstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        System.out.println("====================");
        System.out.println("ĐANG ĐĂNG NHẬP VỚI EMAIL: " + email);
        System.out.println("====================");

        User user = userRepository.findByEmail(email);

        if (user == null) {
            System.out.println("❌ KHÔNG TÌM THẤY USER!");
            throw new UsernameNotFoundException("Không tìm thấy user");
        }

        System.out.println("✅ TÌM THẤY USER: " + user.getEmail());
        System.out.println("🔐 PASSWORD HASH: " + user.getPassword());
        System.out.println("📌 ENABLED: " + user.isEnabled());
        System.out.println("👤 ROLE: " + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),  // enabled
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}