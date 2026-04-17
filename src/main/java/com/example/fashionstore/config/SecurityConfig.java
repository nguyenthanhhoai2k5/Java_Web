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
    // 1. CẤU HÌNH BẢO MẬT CHO ADMIN
    // ==============================================================
    @Bean
    @Order(1) 
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        HttpSessionSecurityContextRepository adminContextRepo = new HttpSessionSecurityContextRepository();
        adminContextRepo.setSpringSecurityContextKey("ADMIN_SECURITY_CONTEXT");

        http
            .securityMatcher("/admin/**") 
            .securityContext(context -> context.securityContextRepository(adminContextRepo))
            .csrf(csrf -> csrf.disable())
            // Hỗ trợ HTTPS trên Railway
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
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
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
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
        HttpSessionSecurityContextRepository userContextRepo = new HttpSessionSecurityContextRepository();
        userContextRepo.setSpringSecurityContextKey("USER_SECURITY_CONTEXT");

        http
            .securityContext(context -> context.securityContextRepository(userContextRepo))
            .csrf(csrf -> csrf.disable())
            // Ép buộc dùng HTTPS để tránh lỗi 403 do Proxy
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
            .authorizeHttpRequests(auth -> auth
                // Thay hasAuthority thành authenticated() để tránh lỗi Role chưa kịp cập nhật từ Google
                .requestMatchers("/profile/**", "/checkout/**").authenticated() 
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
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
