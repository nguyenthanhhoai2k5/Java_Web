package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Mã giảm giá (VD: SUMMER2026)

    private String description; // Tên chương trình/Mô tả

    private Double discountValue; // Giá trị giảm

    private String discountType; // Loại giảm: "percent" hoặc "amount"

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String status; // "Còn hiệu lực", "Hết hạn", "Tạm dừng"

    // Mối quan hệ Nhiều-Nhiều với Category
    @ManyToMany
    @JoinTable(
            name = "coupon_categories",
            joinColumns = @JoinColumn(name = "coupon_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> appliedCategories;
}