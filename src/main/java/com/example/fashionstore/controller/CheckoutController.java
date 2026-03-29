package com.example.fashionstore.controller;

import com.example.fashionstore.model.CartItem;
import com.example.fashionstore.model.Order;
import com.example.fashionstore.service.OrderService;
import com.example.fashionstore.service.ShopCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private ShopCartService shopCartService;

    @Autowired
    private OrderService orderService;

    // Hiển thị trang thanh toán
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session, RedirectAttributes ra) {

        // ĐÃ FIX LỖI ÉP KIỂU TẠI ĐÂY: Dùng new ArrayList<>()
        List<CartItem> cartItems = new ArrayList<>(shopCartService.getCartItems(session));

        // Nếu giỏ hàng trống, không cho vào trang checkout
        if (cartItems.isEmpty()) {
            ra.addFlashAttribute("message", "Giỏ hàng của bạn đang trống!");
            return "redirect:/shop/cart";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", shopCartService.getTotalAmount(session));
        model.addAttribute("totalItems", shopCartService.getTotalItems(session));

        // Gửi object Order rỗng để hứng dữ liệu từ Form
        model.addAttribute("order", new Order());

        return "user_checkout";
    }

    // Xử lý khi bấm nút "ĐẶT HÀNG"
    @PostMapping("/checkout/process")
    public String processCheckout(@ModelAttribute("order") Order order, HttpSession session) {

        // ĐÃ FIX LỖI ÉP KIỂU TẠI ĐÂY: Dùng new ArrayList<>()
        List<CartItem> cartItems = new ArrayList<>(shopCartService.getCartItems(session));
        Double totalAmount = shopCartService.getTotalAmount(session);

        if (!cartItems.isEmpty()) {
            // Gọi Service lưu vào Database
            Order savedOrder = orderService.placeOrder(order, cartItems, totalAmount);

            // Lưu mã đơn hàng vào session để trang Success hiển thị
            session.setAttribute("lastOrderCode", savedOrder.getOrderCode());

            // Làm sạch giỏ hàng (Bạn hãy đảm bảo ShopCartService có hàm clearCart)
            // shopCartService.clearCart(session);

            // Cách xóa giỏ hàng trực tiếp từ session nếu chưa viết hàm clearCart:
            session.removeAttribute("cart");
        }

        return "redirect:/checkout/success";
    }

    // Hiển thị trang Đặt hàng thành công
    @GetMapping("/checkout/success")
    public String checkoutSuccess(Model model, HttpSession session) {
        String orderCode = (String) session.getAttribute("lastOrderCode");
        model.addAttribute("orderCode", orderCode);
        return "user_checkout_success";
    }
}