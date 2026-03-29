package com.example.fashionstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CHỈ CẦN DUY NHẤT BEAN NÀY
    // Spring Security sẽ tự động tìm cái CustomUserDetailsService của bạn (vì nó có @Service)
    // và kết hợp với PasswordEncoder này để tự động tạo ra bộ xác thực!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // ĐÃ XÓA dòng .authenticationProvider() gây lỗi đi!
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/admin/**").permitAll()
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
                // PHẢI CÓ ĐOẠN NÀY THÌ SPRING MỚI TẠO LINK GOOGLE CHO BẠN
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