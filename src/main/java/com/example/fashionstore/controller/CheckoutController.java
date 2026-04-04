package com.example.fashionstore.controller;

import com.example.fashionstore.model.CartItem;
import com.example.fashionstore.model.Coupon;
import com.example.fashionstore.model.Order;
import com.example.fashionstore.model.User;
import com.example.fashionstore.service.CouponService;
import com.example.fashionstore.service.OrderService;
import com.example.fashionstore.service.ShopCartService;
import com.example.fashionstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private ShopCartService shopCartService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    // =========================================================================
    // HÀM HỖ TRỢ: Lấy chính xác Email từ Principal (Hỗ trợ cả Google và Local)
    // =========================================================================
    private String getEmailFromPrincipal(Principal principal) {
        if (principal == null) return null;
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            return oauthToken.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    // 1. HIỂN THỊ TRANG THANH TOÁN
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session, RedirectAttributes ra, Principal principal) {
        List<CartItem> cartItems = new ArrayList<>(shopCartService.getCartItems(session));
        if (cartItems.isEmpty()) {
            ra.addFlashAttribute("message", "Giỏ hàng của bạn đang trống!");
            return "redirect:/shop/cart";
        }

        List<Coupon> activeCoupons = couponService.getActiveCoupons();
        model.addAttribute("activeCoupons", activeCoupons);

        // TỰ ĐỘNG ÁP DỤNG MÃ
        if (session.getAttribute("appliedCode") == null && !activeCoupons.isEmpty()) {
            Coupon bestCoupon = activeCoupons.get(0);
            session.setAttribute("appliedCode", bestCoupon.getCode());
            session.setAttribute("discountValue", bestCoupon.getDiscountValue());
            session.setAttribute("discountType", bestCoupon.getDiscountType());
        }

        Double totalAmount = shopCartService.getTotalAmount(session);
        Double discountAmount = 0.0;

        String appliedCode = (String) session.getAttribute("appliedCode");
        Double discountValue = (Double) session.getAttribute("discountValue");
        String discountType = (String) session.getAttribute("discountType");

        // CHỈ TRỪ TIỀN NẾU MÃ KHÁC "NONE"
        if (appliedCode != null && !"NONE".equals(appliedCode) && discountValue != null) {
            if ("percent".equals(discountType)) {
                discountAmount = totalAmount * (discountValue / 100.0);
            } else if ("amount".equals(discountType)) {
                discountAmount = discountValue;
            }
            if (discountAmount > totalAmount) discountAmount = totalAmount;
            totalAmount = totalAmount - discountAmount;
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", shopCartService.getTotalItems(session));
        model.addAttribute("subTotal", shopCartService.getTotalAmount(session));
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalTotal", totalAmount);

        Order order = new Order();
        if (principal != null) {
            String email = getEmailFromPrincipal(principal); // Dùng hàm hỗ trợ ở đây
            User user = userService.findByEmail(email);
            if (user != null) {
                order.setCustomerName(user.getFullName());
                order.setPhone(user.getPhone());
                order.setEmail(user.getEmail());
                order.setAddress(user.getAddress());
            }
        }
        model.addAttribute("order", order);
        return "user_checkout";
    }

    // 2. XỬ LÝ ÁP DỤNG MÃ GIẢM GIÁ
    @PostMapping("/checkout/apply-coupon")
    public String applyCoupon(@RequestParam(value = "code", defaultValue = "NONE") String code, HttpSession session, RedirectAttributes ra) {
        if ("NONE".equals(code) || code.isEmpty()) {
            session.setAttribute("appliedCode", "NONE");
            session.removeAttribute("discountValue");
            session.removeAttribute("discountType");
            ra.addFlashAttribute("successMsg", "Đã hủy áp dụng mã giảm giá.");
            return "redirect:/checkout";
        }

        Coupon coupon = couponService.getValidCouponByCode(code.trim().toUpperCase());
        if (coupon != null) {
            session.setAttribute("appliedCode", coupon.getCode());
            session.setAttribute("discountValue", coupon.getDiscountValue());
            session.setAttribute("discountType", coupon.getDiscountType());
            ra.addFlashAttribute("successMsg", "Áp dụng mã " + coupon.getCode() + " thành công!");
        } else {
            ra.addFlashAttribute("errorMsg", "Mã giảm giá không hợp lệ hoặc đã hết hạn!");
        }
        return "redirect:/checkout";
    }

    // 3. XỬ LÝ LƯU ĐƠN HÀNG XUỐNG DATABASE
    @PostMapping("/checkout/process")
    public String processCheckout(@ModelAttribute("order") Order order, HttpSession session, Principal principal) {
        List<CartItem> cartItems = new ArrayList<>(shopCartService.getCartItems(session));

        Double finalAmount = shopCartService.getTotalAmount(session);
        String appliedCode = (String) session.getAttribute("appliedCode");
        Double discountValue = (Double) session.getAttribute("discountValue");
        String discountType = (String) session.getAttribute("discountType");

        if (appliedCode != null && !"NONE".equals(appliedCode) && discountValue != null) {
            Double discountAmount = 0.0;
            if ("percent".equals(discountType)) {
                discountAmount = finalAmount * (discountValue / 100.0);
            } else if ("amount".equals(discountType)) {
                discountAmount = discountValue;
            }
            if (discountAmount > finalAmount) discountAmount = finalAmount;
            finalAmount -= discountAmount;
        }

        if (!cartItems.isEmpty()) {
            if (principal != null) {
                String email = getEmailFromPrincipal(principal); // Dùng hàm hỗ trợ ở đây
                User user = userService.findByEmail(email);
                order.setUser(user);
            } else {
                order.setUser(null);
            }

            Order savedOrder = orderService.placeOrder(order, cartItems, finalAmount);
            session.setAttribute("lastOrderCode", savedOrder.getOrderCode());

            // Dọn dẹp Session sau khi đặt hàng thành công
            session.removeAttribute("cart");
            session.removeAttribute("appliedCode");
            session.removeAttribute("discountValue");
            session.removeAttribute("discountType");
        }
        return "redirect:/checkout/success";
    }

    // 4. TRANG HIỂN THỊ ĐẶT HÀNG THÀNH CÔNG
    @GetMapping("/checkout/success")
    public String checkoutSuccess(HttpSession session, Model model) {
        String orderCode = (String) session.getAttribute("lastOrderCode");
        if (orderCode == null) {
            return "redirect:/home";
        }
        model.addAttribute("orderCode", orderCode);
        return "user_checkout_success";
    }
}
