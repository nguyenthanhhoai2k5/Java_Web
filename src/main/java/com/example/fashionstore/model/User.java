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
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String email;

    private String phone;

    private String address;
    // Vai trò: "ADMIN" hoặc "USER"
    private String role;

    // Trạng thái tài khoản (Tùy chọn: true là đang hoạt động)
    private boolean enabled = true;
}