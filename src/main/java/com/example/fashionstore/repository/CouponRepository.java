package com.example.fashionstore.repository;


import com.example.fashionstore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // Tự động có các hàm lưu, xóa, sửa, tìm kiếm
    List<Coupon> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String description);

    // Tìm mã theo tên code chính xác
    Coupon findByCode(String code);
}