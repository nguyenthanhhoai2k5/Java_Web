package com.example.fashionstore.controller;

import com.example.fashionstore.model.Order;
import com.example.fashionstore.model.User;
import com.example.fashionstore.service.OrderService;
import com.example.fashionstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender; // Công cụ gửi mail của Spring Boot

    @Autowired
    private PasswordEncoder passwordEncoder; // Thêm công cụ mã hóa mật khẩu

    // =========================================================================
    // HÀM HỖ TRỢ: Lấy chính xác Email từ Principal (Hỗ trợ cả Google và Local)
    // =========================================================================
    private String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            return oauthToken.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    // 1. Hiển thị trang Đăng nhập
    @GetMapping("/login")
    public String loginPage() {
        return "user_login";
    }

    // 2. Hiển thị form đăng ký (Nếu chưa có)
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User()); // Khởi tạo một User rỗng cho Form
        return "user_register";
    }

    // 3. Xử lý khi nhấn Submit Đăng ký
    @PostMapping("/register/process")
    public String processRegister(@ModelAttribute("user") User user, RedirectAttributes ra) {
        User existingUser = userService.findByEmail(user.getEmail());

        if (existingUser != null) {
            ra.addFlashAttribute("error", "Email này đã được sử dụng! Vui lòng chọn email khác.");
            return "redirect:/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setAuthProvider("LOCAL");
        userService.save(user);

        ra.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }

    // 4. Hiển thị Profile
    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = getEmailFromPrincipal(principal);
        String fullName = "";

        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            fullName = oauthToken.getPrincipal().getAttribute("name");
        } else {
            fullName = email.split("@")[0];
        }

        User user = userService.findByEmail(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setAuthProvider("GOOGLE");
            user.setRole("ROLE_USER");
            userService.save(user);
        }

        List<Order> myOrders = orderService.getOrdersByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("myOrders", myOrders);

        return "user_profile";
    }

    // Xử lý Cập nhật thông tin chữ (Tên, SĐT, Địa chỉ)
    @PostMapping("/profile/update-info")
    public String updateProfileInfo(@ModelAttribute User userForm, Principal principal, RedirectAttributes ra) {
        try {
            User existingUser = userService.findByEmail(getEmailFromPrincipal(principal));

            existingUser.setFullName(userForm.getFullName());
            existingUser.setPhone(userForm.getPhone());
            existingUser.setAddress(userForm.getAddress());

            userService.save(existingUser);
            ra.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật hồ sơ: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // Xử lý Cập nhật riêng Avatar
    @PostMapping("/profile/update-avatar")
    public String updateProfileAvatar(@RequestParam("avatarFile") MultipartFile avatarFile, Principal principal, RedirectAttributes ra) {
        try {
            User existingUser = userService.findByEmail(getEmailFromPrincipal(principal));

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String fileName = "avatar_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static/images/avatars/" + fileName);
                Files.createDirectories(path.getParent());
                Files.copy(avatarFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                existingUser.setAvatar(fileName);
                userService.save(existingUser);
                ra.addFlashAttribute("message", "Cập nhật ảnh đại diện thành công!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // 6. Mở trang Quên mật khẩu
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "user_forgot_password";
    }

    // 7. Xử lý gửi mail mật khẩu mới
    @PostMapping("/forgot-password/process")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes ra) {
        User user = userService.findByEmail(email);

        if (user == null) {
            ra.addFlashAttribute("error", "Email không tồn tại trong hệ thống!");
            return "redirect:/forgot-password";
        }

        String newPassword = UUID.randomUUID().toString().substring(0, 6);
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Khôi phục mật khẩu - Kids Garden");
            message.setText("Chào " + user.getFullName() + ",\n\n"
                    + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                    + "Vui lòng đăng nhập và đổi lại mật khẩu ngay để bảo mật tài khoản nhé.\n"
                    + "Trân trọng,\nKids Garden Team.");

            mailSender.send(message);
            ra.addFlashAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi gửi email. Vui lòng kiểm tra lại thiết lập Email.");
            return "redirect:/forgot-password";
        }

        return "redirect:/login";
    }

    // Xử lý Đổi mật khẩu trong Profile
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes ra) {
        try {
            User user = userService.findByEmail(getEmailFromPrincipal(principal));

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                ra.addFlashAttribute("error", "Tài khoản đăng nhập bằng Google không cần đổi mật khẩu tại đây!");
                return "redirect:/profile";
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/profile";
            }

            if (!newPassword.equals(confirmPassword)) {
                ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/profile";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            ra.addFlashAttribute("message", "Đổi mật khẩu thành công! Lần sau hãy đăng nhập bằng mật khẩu mới nhé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/profile/order-detail/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        // Sử dụng hàm hỗ trợ đã khai báo bên trên
        String email = getEmailFromPrincipal(principal);
        User currentUser = userService.findByEmail(email);

        Order order = orderService.findById(id);

        if (order == null || order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc bạn không có quyền xem!");
            return "redirect:/profile#orders";
        }

        // BỔ SUNG: Gọi Service để lấy chính xác danh sách các món đồ trong đơn hàng này
        List<com.example.fashionstore.model.OrderDetail> orderDetails = orderService.getOrderDetails(id);

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails); // Gửi danh sách ra ngoài HTML

        return "user_order_detail";
    }
}