package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã đơn hàng hiển thị (VD: OD000001)
    private String orderCode;

    private String customerName;
    private String phone;
    private String address;
    private String email;

    private Double totalAmount;

    // Trạng thái: "Đang xử lý", "Đã giao hàng", "Đã hủy"
    private String status;

    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();
}