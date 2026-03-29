package com.example.fashionstore.service;

import com.example.fashionstore.repository.OrderDetailRepository;
import com.example.fashionstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 1. Dữ liệu trạng thái đơn hàng
    public Map<String, List<?>> getOrderStatusData() {
        List<Object[]> data = orderRepository.countOrdersByStatus();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : data) {
            labels.add((String) row[0]);
            values.add((Long) row[1]);
        }
        return createChartDataMap(labels, values);
    }

    // 2. Dữ liệu Top 5 sản phẩm
    public Map<String, List<?>> getTopProductsData() {
        List<Object[]> data = orderDetailRepository.findTopSellingProducts(PageRequest.of(0, 5));
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : data) {
            labels.add((String) row[0]);
            values.add((Long) row[1]);
        }
        return createChartDataMap(labels, values);
    }

    // 3. Dữ liệu Doanh thu theo danh mục
    public Map<String, List<?>> getCategoryRevenueData() {
        List<Object[]> data = orderDetailRepository.getRevenueByCategory();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Object[] row : data) {
            labels.add((String) row[0]);
            values.add((Double) row[1]);
        }
        return createChartDataMap(labels, values);
    }

    // 4. Dữ liệu Doanh thu theo tháng
    public Map<String, List<?>> getMonthlyRevenueData() {
        List<Object[]> data = orderRepository.getRevenueByMonth();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Object[] row : data) {
            labels.add("Tháng " + row[0].toString());
            values.add(((Number) row[1]).doubleValue());
        }
        return createChartDataMap(labels, values);
    }

    // Hàm tiện ích để đóng gói Map
    private Map<String, List<?>> createChartDataMap(List<String> labels, List<?> values) {
        Map<String, List<?>> map = new HashMap<>();
        map.put("labels", labels);
        map.put("values", values);
        return map;
    }
}