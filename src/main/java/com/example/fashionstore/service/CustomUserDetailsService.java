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
        // 1. Tìm user trong Database bằng email (Từ form đăng nhập gửi lên)
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Nếu không tìm thấy email, ném ra lỗi để Spring Security báo "Sai thông tin"
            throw new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
        }

        // 2. Chuyển đổi User của chúng ta thành UserDetails chuẩn của Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // Spring Security sẽ lấy mật khẩu đã băm này để tự động đối chiếu
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())) // Cấp quyền (ROLE_USER hoặc ROLE_ADMIN)
        );
    }
}