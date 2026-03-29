package com.example.fashionstore.controller;

import com.example.fashionstore.model.User;
import com.example.fashionstore.service.CategoryService;
import com.example.fashionstore.service.ProductService;
import com.example.fashionstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    // TRANG CHỦ NGƯỜI DÚNG
    @GetMapping({"/", "/home"})
    public String userHome(Model model) {
        model.addAttribute("newProducts", productService.getNewestProducts());
        model.addAttribute("hotProducts", productService.getBestSellingProducts());
        return "user_index"; // Trả về file user_index.html
    }
    // CHỨC NĂNG ĐĂNG KÝ
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, RedirectAttributes ra) {
        // 1. Cài đặt các giá trị mặc định cho User mới
        user.setEnabled(true);
        user.setRole("USER"); // Mặc định là khách hàng

        // 2. Lưu vào Database
        userService.save(user);

        // 3. Thông báo và chuyển hướng
        ra.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
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