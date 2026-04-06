package com.example.fashionstore.model;

public class CartItem {

    // Mã sản phẩm
    private Long productId;

    // Tên sản phẩm
    private String productName;

    // Giá sản phẩm
    private Double price;

    // Số lượng trong giỏ
    private int quantity;

    private String image;
    public CartItem() {
    }

    public CartItem(Long productId, String productName, Double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Tính tiền của 1 dòng sản phẩm trong giỏ hàng
    public Double getSubtotal() {
        return price * quantity;
    }
}