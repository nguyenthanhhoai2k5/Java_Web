package com.example.fashionstore.controller;

import com.example.fashionstore.model.Coupon;
import com.example.fashionstore.service.CategoryService;
import com.example.fashionstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private com.example.fashionstore.service.CouponService couponService;

    // TRANG CHỦ NGƯỜI DÚNG
    @GetMapping({"/", "/home"})
    public String userHome(Model model) {
        model.addAttribute("newProducts", productService.getNewestProducts());
        model.addAttribute("hotProducts", productService.getBestSellingProducts());
        return "user_index"; // Trả về file user_index.html
    }

    // CHỨC NĂNG 6 & 7: TRANG CỬA HÀNG
    @GetMapping("/shop")
    public String shopPage(@RequestParam(value = "categoryId", required = false) Long categoryId, Model model) {
        // Đổ danh sách Categories ra Sidebar
        model.addAttribute("categories", categoryService.getAll());

        // Lấy danh sách sản phẩm (có lọc theo categoryId nếu người dùng click vào sidebar)
        model.addAttribute("products", productService.getProductsByCategory(categoryId));

        // Đổ dữ liệu cho các Tab Mới nhất / Bán chạy
        model.addAttribute("newProducts", productService.getNewestProducts());
        model.addAttribute("hotProducts", productService.getBestSellingProducts());

        return "user_shop"; // Trả về file user_shop.html
    }

    // TRANG KHUYẾN MÃI
    @GetMapping("/promotions")
    public String promotionsPage(Model model) {
        List<Coupon> activeCoupons = couponService.getActiveCoupons();
        model.addAttribute("activeCoupons", activeCoupons);
        model.addAttribute("saleProducts", productService.getSaleProducts());
        return "user_promotions"; // Trả về file user_promotions.html
    }

    // CHỨC NĂNG 8: TÌM KIẾM
    @GetMapping("/search")
    public String searchProduct(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("products", productService.searchByName(keyword));
        model.addAttribute("keyword", keyword);
        // Tái sử dụng giao diện trang shop để hiển thị kết quả tìm kiếm
        model.addAttribute("categories", categoryService.getAll());
        return "user_shop";
    }
}