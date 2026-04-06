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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tạm thời disable để test logout POST dễ dàng hơn
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/register", "/register/process", "/forgot-password",
                                "/shop/**", "/product/**", "/css/**", "/js/**", "/images/**",
                                "/checkout/success", "/oauth2/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")               // Phải khớp với th:action trong form
                        .logoutSuccessUrl("/login?logout")  // Về trang login kèm tham số logout
                        .invalidateHttpSession(true)        // Xóa Session
                        .deleteCookies("JSESSIONID")        // Xóa Cookie
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}