package com.example.fashionstore.service;

import com.example.fashionstore.model.Product;
import com.example.fashionstore.repository.ShopProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopProductService {

    private final ShopProductRepository shopProductRepository;

    public ShopProductService(ShopProductRepository shopProductRepository) {
        this.shopProductRepository = shopProductRepository;
    }

    public List<Product> findProductsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return shopProductRepository.findAll();
        }

        return shopProductRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    public Product findProductByIdOrThrow(Long productId) {
        return shopProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }
}
