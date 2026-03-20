package com.example.fashionstore.repository;

import com.example.fashionstore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Spring Boot sẽ tự động cung cấp các hàm: save(), findAll(), deleteById(), findById()
}