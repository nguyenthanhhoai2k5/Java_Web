package com.example.fashionstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==============================================================
    // 1. CẤU HÌNH BẢO MẬT CHO ADMIN (Két sắt số 1)
    // ==============================================================
    @Bean
    @Order(1) 
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        // TẠO KHO LƯU TRỮ PHIÊN ĐĂNG NHẬP ĐỘC LẬP CHO ADMIN
        HttpSessionSecurityContextRepository adminContextRepo = new HttpSessionSecurityContextRepository();
        adminContextRepo.setSpringSecurityContextKey("ADMIN_SECURITY_CONTEXT");

        http
            .securityMatcher("/admin/**") 
            // Gắn két sắt số 1 vào chuỗi bảo mật của Admin
            .securityContext(context -> context.securityContextRepository(adminContextRepo))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/admin/login")              
                .loginProcessingUrl("/admin/login")     
                .defaultSuccessUrl("/admin/home", true) 
                .failureUrl("/admin/login?error=true")  
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")             
                .logoutSuccessUrl("/admin/login?logout=true") 
                .permitAll()
            );

        return http.build();
    }

    // ==============================================================
    // 2. CẤU HÌNH BẢO MẬT CHO KHÁCH HÀNG (Két sắt số 2)
    // ==============================================================
    @Bean
    @Order(2) 
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        // TẠO KHO LƯU TRỮ PHIÊN ĐĂNG NHẬP ĐỘC LẬP CHO USER
        HttpSessionSecurityContextRepository userContextRepo = new HttpSessionSecurityContextRepository();
        userContextRepo.setSpringSecurityContextKey("USER_SECURITY_CONTEXT");

        http
            // Gắn két sắt số 2 vào chuỗi bảo mật của Khách hàng
            .securityContext(context -> context.securityContextRepository(userContextRepo))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/profile/**", "/checkout/**").hasAuthority("ROLE_USER") // Chỉ USER mới được vào
                .anyRequest().permitAll() 
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
