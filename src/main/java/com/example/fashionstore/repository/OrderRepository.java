package com.example.fashionstore.repository;

import com.example.fashionstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy danh sách đơn hàng mới nhất lên đầu
    List<Order> findAllByOrderByOrderDateDesc();

    // Tìm kiếm đơn hàng theo mã đơn hoặc số điện thoại
    List<Order> findByOrderCodeContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrderByOrderDateDesc(String orderCode, String phone);

    // Lấy danh sách đơn hàng của một User cụ thể, sắp xếp mới nhất lên trước
    List<Order> findByUserOrderByOrderDateDesc(com.example.fashionstore.model.User user);

    //
    List<Order> findByUserId(Long userId);
    // ==========================================
    // CÁC QUERY DÀNH CHO STATISTIC (THỐNG KÊ)
    // ==========================================

    // 1. Tính tổng doanh thu tháng hiện tại (Loại trừ các đơn Đã hủy)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE MONTH(o.orderDate) = MONTH(CURRENT_DATE()) AND YEAR(o.orderDate) = YEAR(CURRENT_DATE()) AND o.status != 'Đã hủy'")
    Double calculateCurrentMonthRevenue();

    // 2. Đếm số lượng đơn hàng trong tháng hiện tại
    @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.orderDate) = MONTH(CURRENT_DATE()) AND YEAR(o.orderDate) = YEAR(CURRENT_DATE())")
    Long countCurrentMonthOrders();

    // Đếm số lượng đơn hàng theo từng trạng thái (Doughnut Chart)
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    // Lấy doanh thu theo tháng trong năm nay (Line Chart) - Dùng Native Query của MySQL
    @Query(value = "SELECT MONTH(order_date), SUM(total_amount) FROM orders WHERE YEAR(order_date) = YEAR(CURRENT_DATE()) AND status != 'Đã hủy' GROUP BY MONTH(order_date) ORDER BY MONTH(order_date)", nativeQuery = true)
    List<Object[]> getRevenueByMonth();



}