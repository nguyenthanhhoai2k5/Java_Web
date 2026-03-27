package com.example.fashionstore.service;

import com.example.fashionstore.model.OrderDetail;
import com.example.fashionstore.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    /**
     * Tìm kiếm lịch sử mua hàng của một người dùng
     * @param userId ID người dùng
     * @param keyword Tên sản phẩm cần tìm
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách chi tiết đơn hàng thỏa mãn điều kiện
     */
    public List<OrderDetail> findHistory(Long userId, String keyword, LocalDate startDate, LocalDate endDate) {
        // Xử lý keyword rỗng để tránh lỗi SQL
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        return orderDetailRepository.findHistoryWithFilters(userId, searchKeyword, startDate, endDate);
    }
}