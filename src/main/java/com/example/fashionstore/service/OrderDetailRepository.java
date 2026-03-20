package com.example.fashionstore.service;

import com.example.fashionstore.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Truy vấn nâng cao: Lọc theo UserId, Tên sản phẩm và Khoảng thời gian
    @Query("SELECT d FROM OrderDetail d WHERE d.order.user.id = :userId " +
            "AND (:keyword IS NULL OR LOWER(d.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:startDate IS NULL OR d.order.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR d.order.orderDate <= :endDate) " +
            "ORDER BY d.order.orderDate DESC")
    List<OrderDetail> findHistoryWithFilters(@Param("userId") Long userId,
                                             @Param("keyword") String keyword,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}