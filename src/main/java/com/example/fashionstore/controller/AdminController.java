package com.example.fashionstore.controller;

import com.example.fashionstore.model.Category;
import com.example.fashionstore.model.Coupon;
import com.example.fashionstore.model.Order;
import com.example.fashionstore.model.OrderDetail;
import com.example.fashionstore.model.Product;
import com.example.fashionstore.repository.CouponRepository;
import com.example.fashionstore.service.CategoryService;
import com.example.fashionstore.service.CouponService;
import com.example.fashionstore.service.OrderService;
import com.example.fashionstore.service.ProductService;
import com.example.fashionstore.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.fashionstore.service.UserService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin") // Các đường dẫn sẽ bắt đầu bằng /admin
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository; // Giữ lại vì một số logic trước đó của bạn đang dùng trực tiếp

    // Tích hợp Service mới cho chức năng 4 và 5
    @Autowired
    private OrderService orderService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserService userService;
    
    // ==============================================================
    // TRANG ĐĂNG NHẬP ADMIN
    // ==============================================================
    @GetMapping("/login")
    public String showAdminLoginPage() {
        return "admin_login";
    }
    // ==============================================================
    // TRANG CHỦ ADMIN (DASHBOARD)
    // ==============================================================
    @GetMapping({"/", "/home"})
    public String adminHome(Model model) {
        model.addAttribute("page", "home"); // Để active nút Trang chủ trên menu

        // Cập nhật thống kê thực tế cho Dashboard từ StatisticsService
        model.addAttribute("totalOrders", statisticsService.getCurrentMonthOrderCount());
        model.addAttribute("totalSales", statisticsService.getCurrentMonthSalesFormatted());
        model.addAttribute("totalProducts", productService.getAll().size());

        return "admin_home";
    }

    // ==============================================================
    // QUẢN LÝ DANH MỤC & SẢN PHẨM (Chức năng 1)
    // ==============================================================
    @GetMapping("/category")
    public String manageCategory(Model model,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "p", defaultValue = "1") int pageNum){
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("category", new Category());

        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("keyword", keyword);
        }

        Page<Product> page = productService.getAllPaged(pageNum, keyword);

        model.addAttribute("products", page.getContent());
        model.addAttribute("page", "category");
        return "admin_category";
    }

    @PostMapping("/category/save")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryService.save(category);
        return "redirect:/admin/category";
    }

    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return "redirect:/admin/category";
    }

    @GetMapping("/category/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("category", categoryService.getById(id));
        model.addAttribute("page", "category");
        return "admin_category";
    }

    @GetMapping("/product/add")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("page", "category");
        return "admin_product_add";
    }

    @PostMapping("/product/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = imageFile.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static/images/" + fileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(fileName);
            }
            productService.save(product);
            ra.addFlashAttribute("message", "Dữ liệu sản phẩm đã được cập nhật!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/product/edit/{id}")
    public String editProductPage(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        if (product != null) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("page", "category");
            return "admin_product_add";
        }
        return "redirect:/admin/category";
    }

@GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes ra) {
        Product product = productService.getById(id);
        if (product != null) {
            try {
                // Thử xóa sản phẩm
                productService.delete(id);
                
                // Nếu xóa Database thành công thì mới xóa ảnh vật lý
                if (product.getImage() != null) {
                    Path path = Paths.get("src/main/resources/static/images/" + product.getImage());
                    Files.deleteIfExists(path);
                }
                ra.addFlashAttribute("message", "Đã xóa sản phẩm thành công!");
            } catch (Exception e) {
                // Bẫy lỗi 500 do khóa ngoại (Sản phẩm đã nằm trong đơn hàng)
                ra.addFlashAttribute("error", "Không thể xóa! Sản phẩm này đã tồn tại trong lịch sử đơn hàng. Hãy chuyển trạng thái thành 'Cũ' thay vì xóa.");
            }
        }
        return "redirect:/admin/category";
    }

    // ==============================================================
    // QUẢN LÝ MÃ GIẢM GIÁ (Chức năng 2)
    // ==============================================================
    @GetMapping("/coupons")
    public String manageCoupons(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Coupon> coupons;
        if (keyword != null && !keyword.isEmpty()) {
            coupons = couponService.searchCoupons(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            coupons = couponService.getAll();
        }
        model.addAttribute("coupons", coupons);
        model.addAttribute("page", "coupons");
        return "admin_coupon_list";
    }

    @GetMapping("/coupons/add")
    public String addCouponPage(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("page", "coupons");
        return "admin_coupon_add";
    }

    @PostMapping("/coupons/save")
    public String saveCoupon(@ModelAttribute("coupon") Coupon coupon,
                             @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
                             RedirectAttributes ra) {
        try {
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Set<Category> categories = new HashSet<>();
                for (Long id : categoryIds) {
                    Category cat = categoryService.getById(id);
                    if (cat != null) categories.add(cat);
                }
                coupon.setAppliedCategories(categories);
            }
            if (coupon.getStatus() == null) coupon.setStatus("Còn hiệu lực");
            couponService.save(coupon);
            ra.addFlashAttribute("message", "Lưu mã giảm giá thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/coupons/add";
        }
        return "redirect:/admin/coupons";
    }

    @GetMapping("/coupons/edit/{id}")
    public String editCoupon(@PathVariable("id") Long id, Model model) {
        Coupon coupon = couponRepository.findById(id).orElse(null);
        model.addAttribute("coupon", coupon);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("page", "coupons");
        return "admin_coupon_add";
    }

    @GetMapping("/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            couponService.delete(id);
            ra.addFlashAttribute("message", "Đã xóa mã giảm giá thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa mã giảm giá này: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    // ==============================================================
    // CHỨC NĂNG 4: QUẢN LÝ ĐƠN HÀNG
    // ==============================================================
    @GetMapping("/order")
    public String manageOrders(Model model,
                               @RequestParam(value = "keyword", required = false) String keyword) {

        model.addAttribute("page", "orders");

        List<Order> orders;
        if (keyword != null && !keyword.isEmpty()) {
            orders = orderService.searchOrders(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());

        return "admin_order_list";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") String status,
                                    RedirectAttributes ra) {
        try {
            orderService.updateStatus(orderId, status);
            ra.addFlashAttribute("message", "Đã cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/order";
    }

    @GetMapping("/orders/detail/{id}")
    public String viewOrderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderById(id);
        List<OrderDetail> details = orderService.getOrderDetails(id);

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", details);
        model.addAttribute("page", "orders");

        return "admin_order_detail";
    }

    // ==============================================================
    // CHỨC NĂNG 5: BÁO CÁO THỐNG KÊ
    // ==============================================================
    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        model.addAttribute("page", "statistics");

        // 3 Thẻ tổng quan
        model.addAttribute("currentMonthSales", statisticsService.getCurrentMonthSalesFormatted());
        model.addAttribute("currentMonthOrders", statisticsService.getCurrentMonthOrderCount());
        model.addAttribute("bestSellingProduct", statisticsService.getBestSellingProduct());

        // Dữ liệu cho 4 biểu đồ
        model.addAttribute("statusChart", statisticsService.getOrderStatusData());
        model.addAttribute("topProductsChart", statisticsService.getTopProductsData());
        model.addAttribute("categoryChart", statisticsService.getCategoryRevenueData());
        model.addAttribute("monthlyChart", statisticsService.getMonthlyRevenueData());

        return "admin_statistics";
    }

    // ==============================================================
    // QUẢN LÝ KHÁCH HÀNG (Khắc phục lỗi 404)
    // ==============================================================
    @GetMapping({"/customer", "/users"})
    public String manageCustomers(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<com.example.fashionstore.model.User> users;
        
        try {
            if (keyword != null && !keyword.isEmpty()) {
                // Nếu User Service của bạn chưa có hàm search, hãy comment dòng dưới và dùng getAll()
                // users = userService.searchUsers(keyword);
                users = userService.getAllUsers(); // Tạm thời lấy tất cả nếu chưa có hàm tìm kiếm
                model.addAttribute("keyword", keyword);
            } else {
                users = userService.getAllUsers();
            }
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("users", List.of()); // Tránh lỗi Null
        }
        
        model.addAttribute("page", "customer");
        return "admin_user_list";
    }

    // ==============================================================
    // THAO TÁC TRÊN NGƯỜI DÙNG (Xóa / Cập nhật quyền)
    // ==============================================================
    
    // API Cập nhật quyền (Dùng chung với AJAX JavaScript ở giao diện)
    @PostMapping("/users/update-role")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updateUserRole(@RequestParam("id") Long id, @RequestParam("role") String role) {
        com.example.fashionstore.model.User user = userService.findById(id);
        if (user != null) {
            user.setRole(role);
            userService.save(user);
            return org.springframework.http.ResponseEntity.ok().build();
        }
        return org.springframework.http.ResponseEntity.badRequest().build();
    }

    // Xóa tài khoản
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            userService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            // Bẫy lỗi y hệt như sản phẩm: Không cho xóa nếu User đã từng đặt hàng
            ra.addFlashAttribute("errorMessage", "Không thể xóa! Người dùng này đã có lịch sử đặt hàng trong hệ thống.");
        }
        return "redirect:/admin/users";
    }

    // ==============================================================
    // XEM LỊCH SỬ MUA HÀNG CỦA KHÁCH HÀNG
    // ==============================================================
    @GetMapping("/users/history/{id}")
    public String viewUserHistory(@PathVariable("id") Long id, 
                                  @RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "startDate", required = false) String startDate,
                                  @RequestParam(value = "endDate", required = false) String endDate,
                                  Model model) {
        
        // 1. Tìm thông tin khách hàng
        com.example.fashionstore.model.User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/users"; // Nếu không tìm thấy KH thì quay lại trang danh sách
        }
        
        // 2. Đưa dữ liệu sang giao diện admin_user_history.html
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("page", "customer"); // Giữ cho menu "Khách hàng" bên trái luôn sáng

        List<OrderDetail> history = orderService.getHistoryByUserId(id);
        model.addAttribute("history", history);
        
        return "admin_user_history"; 
    }
}