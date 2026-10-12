package com.mycompany.bachhoaxanhonline.module.cart;

import com.mycompany.bachhoaxanhonline.entity.*;
public class CartRequest {

    public static class AddItemRequest {
        private String productId;
        private Double quantity;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }
    }

    public static class UpdateItemRequest {
        private Double quantity;

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }
    }
}
