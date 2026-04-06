package com.example.fashionstore.config; // Đảm bảo dòng này đúng với package của bạn

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối của thư mục src/main/resources/static/images trên hệ thống
        Path imageDir = Paths.get("src/main/resources/static/images");
        String imagePath = imageDir.toFile().getAbsolutePath();

        // Sử dụng "file:" để ép Spring Boot đọc ảnh trực tiếp từ thư mục gốc, bỏ qua thư mục target
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagePath + "/");
    }
}