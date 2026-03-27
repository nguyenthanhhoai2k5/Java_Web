package com.example.fashionstore.service;

import com.example.fashionstore.model.Order;
import com.example.fashionstore.model.OrderDetail;
import com.example.fashionstore.repository.OrderDetailRepository;
import com.example.fashionstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Lấy tất cả đơn hàng
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    // Tìm kiếm đơn hàng
    public List<Order> searchOrders(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByOrderCodeContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrderByOrderDateDesc(keyword, keyword);
        }
        return getAllOrders();
    }

    // Cập nhật trạng thái đơn hàng
    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    // Lấy 1 đơn hàng theo ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // Lấy danh sách sản phẩm trong 1 đơn hàng
    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}