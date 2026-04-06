package com.example.fashionstore.service;

import com.example.fashionstore.model.Coupon;
import com.example.fashionstore.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.stream.Collectors;
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

    // Hàm tìm mã và kiểm tra xem còn hạn không
    public Coupon getValidCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code);
        if (coupon != null) {
            LocalDate today = LocalDate.now();
            // Kiểm tra: Hôm nay phải >= ngày bắt đầu VÀ <= ngày kết thúc
            if (!coupon.getStartDate().isAfter(today) && !coupon.getEndDate().isBefore(today)) {
                return coupon;
            }
        }
        return null; // Trả về null nếu hết hạn hoặc chưa tới ngày
    }

    // Hàm lấy danh sách tất cả các mã đang CÒN HẠN để show lên trang Khuyến Mãi
    public List<Coupon> getActiveCoupons() {
        LocalDate today = LocalDate.now();
        return couponRepository.findAll().stream()
                .filter(c -> !c.getStartDate().isAfter(today) && !c.getEndDate().isBefore(today))
                .collect(Collectors.toList());
    }
}
