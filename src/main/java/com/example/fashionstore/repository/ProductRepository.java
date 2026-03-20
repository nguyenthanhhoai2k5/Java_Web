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
}