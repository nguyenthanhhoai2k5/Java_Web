package com.example.fashionstore.service;

import com.example.fashionstore.model.User;
import com.example.fashionstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Tìm kiếm người dùng theo từ khóa
    public List<User> searchByName(String keyword) {
        return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword, keyword);
    }

    // Lấy thông tin 1 người dùng theo ID
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Lưu hoặc cập nhật người dùng
    public void save(User user) {
        userRepository.save(user);
    }

    // Xóa tài khoản
    public void softDelete(Long id) {
        User user = userRepository.findById(id).get();
        user.setEnabled(false); // Vô hiệu hóa tài khoản
        userRepository.save(user);
    }
}