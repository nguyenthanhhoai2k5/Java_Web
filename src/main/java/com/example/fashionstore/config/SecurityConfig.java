package com.example.fashionstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==============================================================
    // 1. CẤU HÌNH BẢO MẬT CHO ADMIN (Ưu tiên số 1)
    // ==============================================================
    @Bean
    @Order(1) // Đặt ưu tiên 1 để Spring kiểm tra các link /admin trước
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**") // Chuỗi này CHỈ bắt các link bắt đầu bằng /admin
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Cho phép tất cả mọi người vào xem trang đăng nhập của Admin
                .requestMatchers("/admin/login").permitAll()
                // Tất cả các trang /admin/ còn lại BẮT BUỘC phải có quyền ROLE_ADMIN
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/admin/login")              // Link trỏ đến giao diện HTML admin_login
                .loginProcessingUrl("/admin/login")     // Nơi form HTML gửi dữ liệu kiểm tra
                .defaultSuccessUrl("/admin/home", true) // Đăng nhập đúng -> Vào Dashboard
                .failureUrl("/admin/login?error=true")  // Đăng nhập sai -> Tải lại kèm báo lỗi
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")             // Link để đăng xuất tài khoản Admin
                .logoutSuccessUrl("/admin/login?logout=true") // Đăng xuất xong về lại trang login Admin
                .permitAll()
            );

        return http.build();
    }

    // ==============================================================
    // 2. CẤU HÌNH BẢO MẬT CHO KHÁCH HÀNG (Ưu tiên số 2)
    // ==============================================================
    @Bean
    @Order(2) // Các link còn lại sẽ rơi vào chuỗi này
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/profile/**").authenticated() // Vào xem hồ sơ cá nhân phải đăng nhập
                .anyRequest().permitAll() // Còn lại (Trang chủ, xem sản phẩm...) cho phép xem thoải mái
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .permitAll()
            );

        return http.build();
    }
}