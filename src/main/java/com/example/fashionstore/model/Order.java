package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders") @Data
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    private LocalDate orderDate;
    private String status; // "Đã giao", "Đang xử lý", "Đã hủy"

    @OneToMany(mappedBy = "order")
    private List<OrderDetail> details;
}