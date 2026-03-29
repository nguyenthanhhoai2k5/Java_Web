package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer quantity;

    private String status; // Lưu "Mới" hoặc "Cũ"

    private String image; // Lưu tên file ảnh hoặc đường dẫn ảnh
    private double price;
    private Integer soldQuantity = 0;
    // Thiết lập mối quan hệ với bảng Category
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
