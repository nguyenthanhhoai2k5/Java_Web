package com.example.fashionstore.service;

import com.example.fashionstore.model.Product;
import com.example.fashionstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    public List<Product> getAll() {
        return repository.findAll();
    }

    public void save(Product product) {
        repository.save(product);
    }

    // Bổ sung vào ProductService.java
    public Product getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
    // Tìm kiếm sản phẩm
    public List<Product> searchByName(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }
    // Kỹ thuật phân trang
    public Page<Product> getAllPaged(int pageNum, String keyword) {
        // 10 sản phẩm mỗi trang theo yêu cầu của bạn
        Pageable pageable = PageRequest.of(pageNum - 1, 50);

        if (keyword != null && !keyword.isEmpty()) {
            return repository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        return repository.findAll(pageable);
    }
    // ==============================================================
    // CÁC HÀM BỔ SUNG ĐỂ SỬA LỖI "CANNOT FIND SYMBOL" TRONG HomeController
    // ==============================================================

    public List<Product> getProductsByCategory(Long categoryId) {
        if (categoryId == null) {
            return repository.findAll();
        }
        return repository.findByCategoryId(categoryId);
    }

    public List<Product> getNewestProducts() {
        return repository.findTop8ByOrderByIdDesc();
    }

    public List<Product> getBestSellingProducts() {
        // LƯU Ý: Đảm bảo trong class Product.java đã có thuộc tính:
        // private Integer soldQuantity;
        return repository.findTop8ByOrderBySoldQuantityDesc();
    }

    public List<Product> getSaleProducts() {
        // Tìm các sản phẩm có trạng thái "SALE" hoặc "Khuyến mãi"
        return repository.findByStatusIgnoreCase("SALE");
    }
}
