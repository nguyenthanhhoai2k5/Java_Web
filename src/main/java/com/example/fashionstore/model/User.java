package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // Dùng email làm tên đăng nhập

    private String password;
    private String fullName;
    private String phone;
    private String address;
    private String avatar; // Lưu tên file ảnh

    private String role = "ROLE_USER"; // Phân quyền (ROLE_USER, ROLE_ADMIN)

    private String authProvider = "LOCAL"; // Nhận biết đăng nhập thường hay GOOGLE
}