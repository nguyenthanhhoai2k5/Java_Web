package com.example.fashionstore.controller;

import com.example.fashionstore.service.ShopCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ShopCartService shopCartService;

    // Hàm này sẽ tự động chạy và gắn biến "cartCount" vào mọi trang HTML (Thymeleaf)
    @ModelAttribute("cartCount")
    public int populateCartCount(HttpSession session) {
        return shopCartService.getTotalItems(session);
    }
}