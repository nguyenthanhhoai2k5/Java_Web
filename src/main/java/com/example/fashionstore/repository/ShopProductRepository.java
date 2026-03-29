package com.example.fashionstore.repository;

import com.example.fashionstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopProductRepository extends JpaRepository<Product, Long> {

    // Tìm sản phẩm theo tên, không phân biệt chữ hoa/chữ thường
    // Lưu ý:
    // Nếu trong Product.java của bạn field không phải là "name"
    // mà là "productName" thì đổi method thành:
    // List<Product> findByProductNameContainingIgnoreCase(String keyword);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}