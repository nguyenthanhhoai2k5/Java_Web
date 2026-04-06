package com.example.fashionstore.model;

public class CartItem {

    private Long productId;
    private String productName;
    private Double price;
    private int quantity;
    private String image;

    // Constructor không tham số (giữ lại)
    public CartItem() {
    }

    // Constructor có 4 tham số (thêm cái này để fix lỗi)
    public CartItem(Long productId, String productName, Double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.image = null;        // hoặc "" nếu muốn
    }

    // Constructor đầy đủ 5 tham số (giữ nguyên nếu bạn cần)
    public CartItem(Long productId, String productName, Double price, int quantity, String image) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }

    // Getter & Setter (giữ nguyên phần còn lại)
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Double getSubtotal() {
        return price != null ? price * quantity : 0.0;
    }
}