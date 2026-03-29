package com.example.fashionstore.service;

import com.example.fashionstore.repository.OrderDetailRepository;
import com.example.fashionstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // 1. Doanh thu tháng này
    public String getCurrentMonthSalesFormatted() {
        Double revenue = orderRepository.calculateCurrentMonthRevenue();
        if (revenue == null) return "0đ";
        return String.format("%,.0fđ", revenue);
    }

    // 2. Số lượng đơn hàng tháng này
    public Long getCurrentMonthOrderCount() {
        Long count = orderRepository.countCurrentMonthOrders();
        return count != null ? count : 0L;
    }

    // 3. Tên sản phẩm bán chạy nhất
    public String getBestSellingProduct() {
        // Lấy 1 dòng đầu tiên trong danh sách xếp hạng
        List<Object[]> topProducts = orderDetailRepository.findTopSellingProducts(PageRequest.of(0, 1));

        if (topProducts != null && !topProducts.isEmpty()) {
            Object[] bestSeller = topProducts.get(0);
            String productName = (String) bestSeller[0];
            Long totalQty = (Long) bestSeller[1];
            return productName + " (" + totalQty + " cái)";
        }
        return "Chưa có dữ liệu";
    }
}