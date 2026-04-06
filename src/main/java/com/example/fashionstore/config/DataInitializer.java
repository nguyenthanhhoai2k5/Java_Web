package com.example.fashionstore.config;

import com.example.fashionstore.model.User;
import com.example.fashionstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem đã có admin chưa
        User existingAdmin = userRepository.findByEmail("admin@gmail.com");

        if (existingAdmin == null) {
            // Tạo tài khoản admin mới
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // Mật khẩu đã mã hóa
            admin.setFullName("Quản Trị Viên");
            admin.setPhone("0123456789");
            admin.setAddress("Hà Nội, Việt Nam");
            admin.setRole("ROLE_ADMIN");
            admin.setAuthProvider("LOCAL");

            userRepository.save(admin);
            System.out.println("✅ Đã tạo tài khoản Admin thành công!");
            System.out.println("📧 Email: admin@gmail.com");
            System.out.println("🔑 Mật khẩu: admin123");
        } else {
            System.out.println("ℹ️ Tài khoản Admin đã tồn tại!");
        }
    }
}