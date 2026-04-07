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
    // 1. CẤU HÌNH BẢO MẬT CHO ADMIN
    // ==============================================================
    @Bean
    @Order(1) 
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**") 
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
    // 2. CẤU HÌNH BẢO MẬT CHO KHÁCH HÀNG
    // ==============================================================
    @Bean
    @Order(2) 
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Cấp quyền: Khách hàng (USER) hoặc Admin đều có thể xem trang cá nhân
                .requestMatchers("/profile/**", "/checkout/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN") 
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
