package com.mycompany.bachhoaxanhonline.module.cart;

import com.mycompany.bachhoaxanhonline.entity.*;
import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    public static class CartDetailData {
        private String cartId;
        private int totalItems;
        private BigDecimal totalAmount;
        private List<LineItemData> items;

        // Getters and Setters
        public String getCartId() { return cartId; }
        public void setCartId(String cartId) { this.cartId = cartId; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public List<LineItemData> getItems() { return items; }
        public void setItems(List<LineItemData> items) { this.items = items; }
    }

    public static class LineItemData {
        private Long lineItemId;
        private String productId;
        private String productName;
        private String imageUrl;
        private String unit;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal itemTotal;
        private Integer availableStock;

        // Getters and Setters
        public Long getLineItemId() { return lineItemId; }
        public void setLineItemId(Long lineItemId) { this.lineItemId = lineItemId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getItemTotal() { return itemTotal; }
        public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
        public Integer getAvailableStock() { return availableStock; }
        public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    }

    public static class AddItemData {
        private String cartId;
        private Long lineItemId;
        private String productId;
        private Integer quantity;
        private BigDecimal itemTotal;
        private BigDecimal totalCartAmount;

        // Getters and Setters
        public String getCartId() { return cartId; }
        public void setCartId(String cartId) { this.cartId = cartId; }
        public Long getLineItemId() { return lineItemId; }
        public void setLineItemId(Long lineItemId) { this.lineItemId = lineItemId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getItemTotal() { return itemTotal; }
        public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
        public BigDecimal getTotalCartAmount() { return totalCartAmount; }
        public void setTotalCartAmount(BigDecimal totalCartAmount) { this.totalCartAmount = totalCartAmount; }
    }

    public static class UpdateItemData {
        private Long lineItemId;
        private String productId;
        private Integer quantity;
        private BigDecimal itemTotal;
        private BigDecimal totalCartAmount;

        public Long getLineItemId() { return lineItemId; }
        public void setLineItemId(Long lineItemId) { this.lineItemId = lineItemId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getItemTotal() { return itemTotal; }
        public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
        public BigDecimal getTotalCartAmount() { return totalCartAmount; }
        public void setTotalCartAmount(BigDecimal totalCartAmount) { this.totalCartAmount = totalCartAmount; }
    }
}
