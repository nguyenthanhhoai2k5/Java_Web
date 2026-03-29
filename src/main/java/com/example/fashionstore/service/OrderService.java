package com.example.fashionstore.service;

import com.example.fashionstore.model.CartItem;
import com.example.fashionstore.model.Order;
import com.example.fashionstore.model.OrderDetail;
import com.example.fashionstore.model.Product;
import com.example.fashionstore.repository.OrderDetailRepository;
import com.example.fashionstore.repository.OrderRepository;
import com.example.fashionstore.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository; // Bổ sung cái này

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

    @Transactional
    public Order placeOrder(Order order, List<CartItem> cartItems, Double totalAmount) {
        // 1. Sinh mã đơn hàng ngẫu nhiên và thiết lập thông tin cơ bản
        order.setOrderCode("OD" + System.currentTimeMillis());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Đang xử lý");
        order.setTotalAmount(totalAmount);

        // 2. Lưu Order vào bảng orders để lấy ID
        Order savedOrder = orderRepository.save(order);

        // 3. Duyệt qua giỏ hàng để lưu chi tiết và trừ kho
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);

            // Tìm Product thật trong DB
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                detail.setProduct(product);

                // === BẮT ĐẦU CẬP NHẬT KHO ===
                // Trừ số lượng trong kho
                int newQuantity = product.getQuantity() - item.getQuantity();
                product.setQuantity(Math.max(newQuantity, 0)); // Đảm bảo kho không bị số âm

                // Tăng số lượng đã bán (phục vụ chức năng Bán chạy nhất)
                int currentSold = product.getSoldQuantity() != null ? product.getSoldQuantity() : 0;
                product.setSoldQuantity(currentSold + item.getQuantity());

                // Lưu cập nhật sản phẩm vào database
                productRepository.save(product);
                // === KẾT THÚC CẬP NHẬT KHO ===
            }

            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPrice());
            // Sử dụng getSubtotal() từ class CartItem của bạn
            detail.setSubTotal(item.getSubtotal());

            orderDetailRepository.save(detail);
        }

        return savedOrder;
    }
}