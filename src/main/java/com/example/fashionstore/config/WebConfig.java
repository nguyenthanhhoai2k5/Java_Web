package com.example.fashionstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối tới thư mục chứa ảnh trong máy
        Path imageDir = Paths.get("src/main/resources/static/images");
        String imagePath = imageDir.toFile().getAbsolutePath();

        // Ép Spring Boot: Bất cứ khi nào đường dẫn web có chữ /images/ thì hãy tìm thẳng vào thư mục gốc này
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagePath + "/");
    }
}