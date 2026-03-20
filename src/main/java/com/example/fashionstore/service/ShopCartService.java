package com.example.fashionstore.service;

import com.example.fashionstore.model.CartItem;
import com.example.fashionstore.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShopCartService {

    private static final String CART_SESSION_KEY = "SHOPPING_CART";

    private final ShopProductService shopProductService;

    public ShopCartService(ShopProductService shopProductService) {
        this.shopProductService = shopProductService;
    }

    public Collection<CartItem> getCartItems(HttpSession session) {
        return getOrCreateCart(session).values();
    }

    public void addItem(Long productId, int quantity, HttpSession session) {
        int normalizedQuantity = quantity > 0 ? quantity : 1;
        Product product = shopProductService.findProductByIdOrThrow(productId);
        Map<Long, CartItem> cart = getOrCreateCart(session);

        if (cart.containsKey(productId)) {
            CartItem existingItem = cart.get(productId);
            existingItem.setQuantity(existingItem.getQuantity() + normalizedQuantity);
        } else {
            CartItem cartItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    normalizedQuantity
            );
            cart.put(productId, cartItem);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateItemQuantity(Long productId, int quantity, HttpSession session) {
        Map<Long, CartItem> cart = getOrCreateCart(session);

        if (!cart.containsKey(productId)) {
            return;
        }

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.get(productId).setQuantity(quantity);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeItem(Long productId, HttpSession session) {
        Map<Long, CartItem> cart = getOrCreateCart(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public double getTotalAmount(HttpSession session) {
        return getCartItems(session).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public int getTotalItems(HttpSession session) {
        return getCartItems(session).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, CartItem> getOrCreateCart(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);

        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }

        return cart;
    }
}
