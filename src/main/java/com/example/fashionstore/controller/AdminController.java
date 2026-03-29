package com.example.fashionstore.controller;

import com.example.fashionstore.model.*;
import com.example.fashionstore.repository.CouponRepository;
import com.example.fashionstore.repository.UserRepository;
import com.example.fashionstore.service.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private UserRepository userRepository;

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
                Path path = Paths.get("src/main/resources/static/images/" + product.getImage());
                Files.deleteIfExists(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            productService.delete(id);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm thành công!");
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

    //==============================================================
    // CHỨC NĂNG 3: QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG
    //==============================================================
    @GetMapping("/customer")
    public String listUsers(@RequestParam(name = "keyword", required = false) String keyword,
                            Model model) {
        List<User> users;

        if (keyword != null && !keyword.isEmpty()) {
            users = userService.searchByName(keyword);
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);

        // SỬA Ở ĐÂY: Phải khớp hoàn toàn với tên file admin_user_list.html
        return "admin_user_list";
    }

    // Tiện tay viết luôn hàm xử lý Update Role cho cái Script fetch của bạn
    @PostMapping("/admin/update-role")
    @ResponseBody
    public ResponseEntity<String> updateRole(@RequestParam Long id, @RequestParam String role) {
        User user = userService.getById(id);
        if (user != null) {
            user.setRole(role);
            userService.save(user);
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    // xóa tài khoản
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getById(id);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/admin/customer";
        }

        // 1. Kiểm tra nếu là ADMIN
        if ("ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản Admin không thể xóa!");
            return "redirect:/admin/customer";
        }

        // 2. Kiểm tra nếu User đã có đơn hàng (Cần tiêm OrderRepository hoặc OrderService vào)
        // Giả sử trong User model bạn có List<Order> orders
        if (user.getOrders() != null && !user.getOrders().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng này đã có đơn hàng, không thể xóa để tránh mất dữ liệu lịch sử!");
            return "redirect:/admin/customer";
        }

        // 3. Nếu thỏa mãn điều kiện thì tiến hành xóa
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/admin/customer";
    }
    // Xem lịch sử mua hàng
    @GetMapping("/users/history/{id}")
    public String viewUserHistory(@PathVariable Long id,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  Model model) {
        User user = userService.getById(id);
        if (user == null) {
            return "redirect:/admin/customer";
        }

        // Lấy danh sách chi tiết mua hàng
        List<OrderDetail> history = orderService.getHistoryByUserId(id);

        model.addAttribute("user", user);
        model.addAttribute("history", history);
        model.addAttribute("keyword", keyword); // Để giữ lại giá trị trong ô search

        // Trả về tên file HTML (giả sử bạn đặt tên là admin_user_history.html)
        return "admin_user_history";
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

        // Gọi Service để lấy dữ liệu thống kê đẩy ra View
        model.addAttribute("currentMonthSales", statisticsService.getCurrentMonthSalesFormatted());
        model.addAttribute("currentMonthOrders", statisticsService.getCurrentMonthOrderCount());
        model.addAttribute("bestSellingProduct", statisticsService.getBestSellingProduct());

        return "admin_statistics";
    }
}