package com.example.fashionstore.service;

import com.example.fashionstore.model.User;
import com.example.fashionstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    // --- CÁC HÀM BỔ SUNG CHO ADMIN ---

    // 1. Lấy toàn bộ danh sách User
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Tìm kiếm User theo tên hoặc email
    public List<User> searchUsers(String keyword) {
        return userRepository.searchByKeyword(keyword);
    }

    // 3. Tìm User theo ID (Dùng để sửa quyền/xóa)
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // 4. Xóa User
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}