package com.example.fashionstore.controller;

import com.example.fashionstore.service.ShopCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shop/cart")
public class ShopCartController {

    private final ShopCartService shopCartService;

    public ShopCartController(ShopCartService shopCartService) {
        this.shopCartService = shopCartService;
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        model.addAttribute("cartItems", shopCartService.getCartItems(session));
        model.addAttribute("totalAmount", shopCartService.getTotalAmount(session));
        model.addAttribute("totalItems", shopCartService.getTotalItems(session));

        // Đã sửa dòng này để trỏ đúng vào file user_cart.html trong thư mục templates
        return "user_cart";
    }

    @PostMapping("/add")
    public String addCartItem(@RequestParam("productId") Long productId,
                              @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        shopCartService.addItem(productId, quantity, session);
        redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm vào giỏ hàng");
        return "redirect:/shop/cart"; // Giữ nguyên vì đây là URL redirect
    }

    @PostMapping("/update")
    public String updateCartItemQuantity(@RequestParam("productId") Long productId,
                                         @RequestParam("quantity") int quantity,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        shopCartService.updateItemQuantity(productId, quantity, session);
        redirectAttributes.addFlashAttribute("message", "Đã cập nhật giỏ hàng");
        return "redirect:/shop/cart"; // Giữ nguyên vì đây là URL redirect
    }

    @PostMapping("/remove")
    public String removeCartItem(@RequestParam("productId") Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        shopCartService.removeItem(productId, session);
        redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng");
        return "redirect:/shop/cart"; // Giữ nguyên vì đây là URL redirect
    }
}