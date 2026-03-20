package com.example.fashionstore.service;

import com.example.fashionstore.model.Coupon;
import com.example.fashionstore.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    public void save(Coupon coupon) {
        // Logic bổ sung: Tự động cập nhật trạng thái dựa trên ngày tháng nếu cần
        couponRepository.save(coupon);
    }

    public List<Coupon> getAll() {
        return couponRepository.findAll();
    }

    // hàm xóa sản phẩm
    public void delete(Long id) {
        couponRepository.deleteById(id);
    }

    // Hàm tìm kiếm khuyến mãi
    public List<Coupon> searchCoupons(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return couponRepository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        }
        return couponRepository.findAll();
    }
}
