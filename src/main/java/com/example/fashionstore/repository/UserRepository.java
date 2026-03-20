package com.example.fashionstore.repository;

import com.example.fashionstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm kiếm theo tên đăng nhập hoặc tên đầy đủ (không phân biệt hoa thường)
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(String username, String fullName);
}