package com.example.fashionstore.controller;

import com.example.fashionstore.service.ShopCartService;
import com.example.fashionstore.service.ShopProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/shop/products")
public class ShopProductController {

    private final ShopProductService shopProductService;
    private final ShopCartService shopCartService;

    public ShopProductController(ShopProductService shopProductService,
                                 ShopCartService shopCartService) {
        this.shopProductService = shopProductService;
        this.shopCartService = shopCartService;
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               Model model,
                               HttpSession session) {
        model.addAttribute("products", shopProductService.findProductsByKeyword(keyword));
        model.addAttribute("keyword", keyword);
        addCartSummary(model, session);
        return "shop/products";
    }

    @GetMapping("/{productId}")
    public String showProductDetail(@PathVariable("productId") Long productId,
                                    Model model,
                                    HttpSession session) {
        model.addAttribute("product", shopProductService.findProductByIdOrThrow(productId));
        addCartSummary(model, session);
        return "user_product_detail";
    }

    private void addCartSummary(Model model, HttpSession session) {
        model.addAttribute("cartCount", shopCartService.getTotalItems(session));
    }
}
