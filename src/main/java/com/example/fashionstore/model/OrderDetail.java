package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_details") @Data
public class OrderDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private Double priceAtPurchase; // Lưu giá lúc mua để tránh thay đổi sau này
}