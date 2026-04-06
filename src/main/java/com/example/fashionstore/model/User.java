package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    private String username;

    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    private String address;
    private String avatar; // Lưu tên file ảnh

    private String role = "ROLE_USER"; // Phân quyền (ROLE_USER, ROLE_ADMIN)

    @Column(name = "auth_provider")
    private String authProvider;

    @Column(nullable = false)
    private boolean enabled = true;

    // Sửa lýddoweoen hàng
    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();  // ✅ THÊM DÒNG NÀY

    public List<Order> getOrders() {  // ✅ THÊM GETTER
        return orders;
    }

    public void setOrders(List<Order> orders) {  // ✅ THÊM SETTER
        this.orders = orders;
    }
}