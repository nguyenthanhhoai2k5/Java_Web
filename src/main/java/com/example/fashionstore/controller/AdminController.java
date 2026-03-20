package com.example.fashionstore.controller;

import com.example.fashionstore.model.*;
import com.example.fashionstore.repository.CategoryRepository;
import com.example.fashionstore.repository.CouponRepository;
import com.example.fashionstore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin") // Các đường dẫn sẽ bắt đầu bằng /admin
public class AdminController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired // Phải có thêm dòng này
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Autowired
    OrderService orderService;

    // Home
    @GetMapping({"/", "/home"})
    public String adminHome(Model model) {
        model.addAttribute("page", "home"); // Để active nút Trang chủ trên menu
        return "admin_home";
    }

    @GetMapping("/category") // Điều hướng trang đến Quản lý danh mục
    public String manageCategory(Model model,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "p", defaultValue = "1") int pageNum){
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("category", new Category());

        List<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchByName(keyword);
            model.addAttribute("keyword", keyword); // Gửi lại keyword để hiển thị trên ô nhập
        } else {
            products = productService.getAll();
        }

        // Gọi hàm phân trang từ Service
        Page<Product> page = productService.getAllPaged(pageNum, keyword);

        model.addAttribute("products", page.getContent()); // Danh sách sp trên trang này
        model.addAttribute("products", products);
        model.addAttribute("page", "category");
        return "admin_category";
    }

    // Điều hướng đến trang danh sách mã giảm giá
    @GetMapping("/coupons")
    public String manageCoupons(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Coupon> coupons;

        if (keyword != null && !keyword.isEmpty()) {
            coupons = couponService.searchCoupons(keyword);
            model.addAttribute("keyword", keyword); // Gửi lại keyword để hiển thị ở ô nhập
        } else {
            coupons = couponService.getAll();
        }

        model.addAttribute("coupons", coupons);
        model.addAttribute("page", "coupons");
        return "admin_coupon_list";
    }

    // Điều hướng đến trang thêm mã giảm giá
    @GetMapping("/coupons/add")
    public String addCouponPage(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("categories", categoryService.getAll()); // Để chọn loại SP áp dụng
        model.addAttribute("page", "coupons");
        return "admin_coupon_add";
    }

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        model.addAttribute("page", "statistics");
        return "admin_statistics";
    }

    // Thêm
    @PostMapping("/category/save")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryService.save(category); // JPA tự hiểu: có ID là Update, không ID là Insert
        return "redirect:/admin/category";
    }

    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return "redirect:/admin/category";
    }

    // Chỉnh sửa
    @GetMapping("/category/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("category", categoryService.getById(id)); // Lấy dữ liệu cũ để sửa
        model.addAttribute("page", "category");
        return "admin_category";
    }

    // Lưu sản phẩm va database:
    @PostMapping("/product/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                // Có upload ảnh mới -> Lưu ảnh mới
                String fileName = imageFile.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static/images/" + fileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(fileName);
            }
            // Nếu imageFile trống, đối tượng 'product' đã có sẵn tên ảnh cũ từ input hidden

            productService.save(product);
            ra.addFlashAttribute("message", "Dữ liệu sản phẩm đã được cập nhật!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }
    //  Mở trang thêm sản phẩm
    @GetMapping("/product/add")
    public String addProductPage(Model model) {
        // 1. Phải gửi một đối tượng Product trống để Thymeleaf liên kết (bind) dữ liệu form
        model.addAttribute("product", new Product());

        // 2. Gửi danh sách categories để đổ vào Selectbox
        model.addAttribute("categories", categoryService.getAll());

        model.addAttribute("page", "category");
        return "admin_product_add";
    }

    // 1. Mở trang chỉnh sửa sản phẩm
    @GetMapping("/product/edit/{id}")
    public String editProductPage(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        if (product != null) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("page", "category");
            return "admin_product_add"; // Dùng chung form với trang Thêm
        }
        return "redirect:/admin/category";
    }

    // 2. Xử lý xóa sản phẩm
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes ra) {
        Product product = productService.getById(id);
        if (product != null) {
            // (Tùy chọn) Xóa file ảnh vật lý trong thư mục static/images nếu muốn
            try {
                Path path = Paths.get("src/main/resources/static/images/" + product.getImage());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            productService.delete(id);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm thành công!");
        }
        return "redirect:/admin/category";
    }
    // Lưu mã giảm giá vào database
    @PostMapping("/coupons/save")
    public String saveCoupon(@ModelAttribute("coupon") Coupon coupon,
                             @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
                             RedirectAttributes ra) {
        try {
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Set<Category> categories = new HashSet<>();
                for (Long id : categoryIds) {
                    Category cat = categoryService.getById(id); // Lấy category thật từ DB
                    if (cat != null) categories.add(cat);
                }
                coupon.setAppliedCategories(categories);
            }

            // Đảm bảo các trường không bị null nếu form để trống
            if (coupon.getStatus() == null) coupon.setStatus("Còn hiệu lực");

            couponRepository.save(coupon); // Lưu vào DB
            ra.addFlashAttribute("message", "Lưu mã giảm giá thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // Xem lỗi chi tiết ở console IntelliJ
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/coupons/add";
        }
        return "redirect:/admin/coupons";
    }
    // Chỉnh sửa mã giảm giá.
    @GetMapping("/coupons/edit/{id}")
    public String editCoupon(@PathVariable("id") Long id, Model model) {
        Coupon coupon = couponRepository.findById(id).orElse(null);
        model.addAttribute("coupon", coupon);
        model.addAttribute("categories", categoryService.getAll());
        return "admin_coupon_add";
    }
    // Xóa mã giảm giá
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

    // ---------------------------------------------------------------------------------------
    // LẤY THÔNG TIN NGƯƠI DÙNG
    //----------------------------------------------------------------------------------------
    @GetMapping("/users")   // Cho phép điều hướng trang user
    public String manageUsers(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userService.searchByName(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("page", "users");
        return "admin_user_list";
    }
    //  Hàm cho phép admin có thẻ phân quền trực tiếp
    @PostMapping("/users/update-role")
    @ResponseBody // Trả về text/json thay vì chuyển hướng trang
    public String updateRole(@RequestParam("id") Long id, @RequestParam("role") String role) {
        try {
            User user = userService.getById(id);
            if (user != null) {
                user.setRole(role);
                userService.save(user); // Cập nhật vào DB
                return "Success";
            }
        } catch (Exception e) {
            return "Error";
        }
        return "Fail";
    }

    // Xem lịch sử của người dùng
    @GetMapping("/users/history/{id}")
    public String viewUserHistory(@PathVariable("id") Long id,
                                  @RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  Model model) {
        User user = userService.getById(id);
        // Logic: Tìm kiếm chi tiết đơn hàng của User này
        // Ở đây tôi giả định bạn viết hàm findHistory trong OrderService
        List<OrderDetail> history = orderService.findHistory(id, keyword, startDate, endDate);

        model.addAttribute("user", user);
        model.addAttribute("history", history);
        model.addAttribute("page", "users");
        return "admin_user_history";
    }
    // XÓA TÀI KHOẢN
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            userService.softDelete(id);
            ra.addFlashAttribute("message", "Đã xóa tài khoản thành công!");
        } catch (Exception e) {
            // Nếu lỗi xảy ra (thường do còn đơn hàng liên quan), báo lỗi cho Admin
            ra.addFlashAttribute("error", "Không thể xóa tài khoản này vì đã có dữ liệu mua hàng liên quan!");
            e.printStackTrace();
        }
        return "redirect:/admin/users";
    }


    // Bạn có thể thêm các @GetMapping khác cho Coupon, Customer, Order tương tự
}