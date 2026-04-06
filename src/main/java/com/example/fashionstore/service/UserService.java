package com.example.fashionstore.service;

import com.example.fashionstore.model.User;
import com.example.fashionstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Tìm kiếm người dùng theo từ khóa (tìm theo tên hoặc email)
    public List<User> searchByName(String keyword) {
        // Sửa lại: tìm theo fullName hoặc email
        return userRepository.findByFullNameContainingIgnoreCase(keyword);
    }

    // Lấy thông tin 1 người dùng theo ID
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Lưu hoặc cập nhật người dùng
    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("Không tìm thấy người dùng để xóa!");
        }
    }
    // Tìm email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}