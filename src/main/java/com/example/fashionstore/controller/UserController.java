package com.example.fashionstore.controller;

import com.example.fashionstore.model.Order;
import com.example.fashionstore.model.OrderDetail;
import com.example.fashionstore.model.User;
import com.example.fashionstore.service.OrderService;
import com.example.fashionstore.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final OrderService orderService;
    private final UserService userService;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection - Fix field injection warning
    public UserController(OrderService orderService,
                          UserService userService,
                          JavaMailSender mailSender,
                          PasswordEncoder passwordEncoder) {  // ✅ Dùng @Qualifier
        this.orderService = orderService;
        this.userService = userService;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // HÀM HỖ TRỢ: Lấy chính xác Email từ Principal (Hỗ trợ cả Google và Local)
    // =========================================================================
    private String getEmailFromPrincipal(Principal principal) {
        if (principal == null) return null;

        // Fix: Pattern matching Java 16+ và kiểm tra NullPointerException
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
            var attributes = oauthToken.getPrincipal().getAttributes();
            if (attributes != null && attributes.containsKey("email")) {
                return String.valueOf(attributes.get("email"));
            }
            return null;
        }
        return principal.getName();
    }

    // Lấy tên đầy đủ từ Principal
    private String getFullNameFromPrincipal(Principal principal) {
        if (principal == null) return null;

        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
            var attributes = oauthToken.getPrincipal().getAttributes();
            if (attributes != null && attributes.containsKey("name")) {
                return String.valueOf(attributes.get("name"));
            }
            return null;
        }
        return principal.getName().split("@")[0];
    }

    // 1. Hiển thị trang Đăng nhập
    @GetMapping("/login")
    public String loginPage() {
        return "user_login";
    }

    // 2. Hiển thị form đăng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "user_register";
    }

    // 3. Xử lý khi nhấn Submit Đăng ký
    @PostMapping("/register/process")
    @Transactional
    public String processRegister(@ModelAttribute("user") User user, RedirectAttributes ra) {
        System.out.println("====================");
        System.out.println("📝 BẮT ĐẦU ĐĂNG KÝ");
        System.out.println("📧 Email: " + user.getEmail());
        System.out.println("====================");

        try {
            // Kiểm tra email đã tồn tại chưa
            User existingUser = userService.findByEmail(user.getEmail());
            if (existingUser != null) {
                ra.addFlashAttribute("error", "Email này đã được sử dụng! Vui lòng chọn email khác.");
                return "redirect:/register";
            }

            // Kiểm tra mật khẩu
            if (user.getPassword() == null || user.getPassword().length() < 6) {
                ra.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
                return "redirect:/register";
            }

            // Thiết lập thông tin user
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            user.setAuthProvider("LOCAL");
            user.setEnabled(true);

            // ✅ QUAN TRỌNG: Set username bằng email (vì cột username không được NULL)
            user.setUsername(user.getEmail());

            // Log để kiểm tra
            System.out.println("💾 Đang lưu user với username: " + user.getUsername());

            // Lưu user
            userService.save(user);

            System.out.println("✅ ĐĂNG KÝ THÀNH CÔNG: " + user.getEmail());

            ra.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";

        } catch (Exception e) {
            System.err.println("❌ LỖI KHI ĐĂNG KÝ: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi đăng ký: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // 4. Hiển thị Profile
    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            String email = getEmailFromPrincipal(principal);
            if (email == null) {
                ra.addFlashAttribute("error", "Không thể xác định email. Vui lòng đăng nhập lại!");
                return "redirect:/login";
            }

            String fullName = getFullNameFromPrincipal(principal);
            User user = userService.findByEmail(email);

            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setFullName(fullName != null ? fullName : email.split("@")[0]);
                user.setAuthProvider("GOOGLE");
                user.setRole("ROLE_USER");
                userService.save(user);
            }

            // Fix: Gọi đúng method - truyền userId thay vì user object
            List<Order> myOrders = orderService.getOrdersByUserId(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("myOrders", myOrders);

            return "user_profile";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị profile: ", e);
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi tải thông tin profile!");
            return "redirect:/login";
        }
    }

    // Xử lý Cập nhật thông tin chữ (Tên, SĐT, Địa chỉ)
    @PostMapping("/profile/update-info")
    public String updateProfileInfo(@ModelAttribute User userForm, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            String email = getEmailFromPrincipal(principal);
            if (email == null) {
                ra.addFlashAttribute("error", "Không thể xác định email!");
                return "redirect:/profile";
            }

            User existingUser = userService.findByEmail(email);
            if (existingUser == null) {
                ra.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/profile";
            }

            existingUser.setFullName(userForm.getFullName());
            existingUser.setPhone(userForm.getPhone());
            existingUser.setAddress(userForm.getAddress());

            userService.save(existingUser);
            ra.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            logger.error("Lỗi cập nhật thông tin: ", e);
            ra.addFlashAttribute("error", "Lỗi cập nhật hồ sơ: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // Xử lý Cập nhật riêng Avatar
    @PostMapping("/profile/update-avatar")
    public String updateProfileAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                                      Principal principal,
                                      RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            String email = getEmailFromPrincipal(principal);
            if (email == null) {
                ra.addFlashAttribute("error", "Không thể xác định email!");
                return "redirect:/profile";
            }

            User existingUser = userService.findByEmail(email);
            if (existingUser == null) {
                ra.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/profile";
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String fileName = "avatar_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/images/avatars/");
                Files.createDirectories(uploadPath);
                Path path = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                existingUser.setAvatar(fileName);
                userService.save(existingUser);
                ra.addFlashAttribute("message", "Cập nhật ảnh đại diện thành công!");
            } else {
                ra.addFlashAttribute("error", "Vui lòng chọn file ảnh để upload!");
            }
        } catch (Exception e) {
            logger.error("Lỗi upload ảnh: ", e);
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
        try {
            User user = userService.findByEmail(email);

            if (user == null) {
                ra.addFlashAttribute("error", "Email không tồn tại trong hệ thống!");
                return "redirect:/forgot-password";
            }

            // Kiểm tra nếu là tài khoản Google thì không cho đổi mật khẩu
            if ("GOOGLE".equals(user.getAuthProvider())) {
                ra.addFlashAttribute("error", "Tài khoản Google đăng nhập bằng email, không thể đổi mật khẩu tại đây!");
                return "redirect:/forgot-password";
            }

            String newPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Khôi phục mật khẩu - Fashion Store");
            message.setText("Chào " + (user.getFullName() != null ? user.getFullName() : user.getEmail()) + ",\n\n"
                    + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                    + "Vui lòng đăng nhập và đổi lại mật khẩu ngay để bảo mật tài khoản nhé.\n\n"
                    + "Trân trọng,\nFashion Store Team.");

            mailSender.send(message);
            ra.addFlashAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn!");

        } catch (Exception e) {
            logger.error("Lỗi gửi email khôi phục mật khẩu: ", e);
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
        if (principal == null) return "redirect:/login";

        try {
            String email = getEmailFromPrincipal(principal);
            if (email == null) {
                ra.addFlashAttribute("error", "Không thể xác định email!");
                return "redirect:/profile";
            }

            User user = userService.findByEmail(email);
            if (user == null) {
                ra.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/profile";
            }

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

            if (newPassword.length() < 6) {
                ra.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return "redirect:/profile";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            ra.addFlashAttribute("message", "Đổi mật khẩu thành công! Lần sau hãy đăng nhập bằng mật khẩu mới nhé.");
        } catch (Exception e) {
            logger.error("Lỗi đổi mật khẩu: ", e);
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/profile/order-detail/{id}")
    public String orderDetail(@PathVariable Long id,
                              Model model,
                              Principal principal,
                              RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            String email = getEmailFromPrincipal(principal);
            if (email == null) {
                ra.addFlashAttribute("error", "Không thể xác định email!");
                return "redirect:/profile";
            }

            User currentUser = userService.findByEmail(email);
            if (currentUser == null) {
                ra.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/login";
            }

            Order order = orderService.findById(id);

            if (order == null) {
                ra.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
                return "redirect:/profile#orders";
            }

            if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
                ra.addFlashAttribute("error", "Bạn không có quyền xem đơn hàng này!");
                return "redirect:/profile#orders";
            }

            List<OrderDetail> orderDetails = orderService.getOrderDetails(id);

            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails);

            return "user_order_detail";
        } catch (Exception e) {
            logger.error("Lỗi hiển thị chi tiết đơn hàng: ", e);
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi tải thông tin đơn hàng!");
            return "redirect:/profile#orders";
        }
    }
}