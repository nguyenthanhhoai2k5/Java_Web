package com.example.fashionstore.repository;

import com.example.fashionstore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Các phương thức cơ bản sẽ được kế thừa tự động
    List<Product> findByNameContainingIgnoreCase(String name);
    // Trong ProductRepository.java
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // --- CÁC HÀM BỔ SUNG CHO TRANG NGƯỜI DÙNG ---
    List<Product> findByCategoryId(Long categoryId);

    List<Product> findTop8ByOrderByIdDesc(); // Lấy 8 SP mới nhất

    List<Product> findTop8ByOrderBySoldQuantityDesc(); // Lấy 8 SP bán chạy nhất

    List<Product> findByStatusIgnoreCase(String status); // Lọc SP khuyến mãi
}