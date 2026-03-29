package com.example.fashionstore.repository;

import com.example.fashionstore.model.OrderDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Lấy danh sách chi tiết của một đơn hàng cụ thể
    List<OrderDetail> findByOrderId(Long orderId);

    // ==========================================
    // QUERY TÌM SẢN PHẨM BÁN CHẠY NHẤT (THỐNG KÊ)
    // ==========================================
    @Query("SELECT p.name, SUM(od.quantity) as totalQty FROM OrderDetail od JOIN od.product p GROUP BY p.id, p.name ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}