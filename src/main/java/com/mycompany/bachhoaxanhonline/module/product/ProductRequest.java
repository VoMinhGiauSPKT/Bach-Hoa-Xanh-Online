package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductRequest {

    public static class ProductFilterQuery {
        private Integer page = 1;
        private Integer limit = 12;
        private String keyword;
        private String categoryId;
        private String sortBy;
        private Boolean inStock;

        public ProductFilterQuery() {
        }

        public ProductFilterQuery(Integer page, Integer limit, String keyword, String categoryId, String sortBy, Boolean inStock) {
            this.page = page != null ? page : 1;
            this.limit = limit != null ? limit : 12;
            this.keyword = keyword;
            this.categoryId = categoryId;
            this.sortBy = sortBy;
            this.inStock = inStock;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }

        public Boolean getInStock() {
            return inStock;
        }

        public void setInStock(Boolean inStock) {
            this.inStock = inStock;
        }
    }

    public static class CreateProductRequest {
        private String productId;
        private String productName;
        private String imageUrl;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        private String categoryId;
        private String supplierId;
        private BigDecimal importPrice;
        private Double vat;
        private BigDecimal sellingPrice;
        private String unit;
        private Integer quantity;

        public CreateProductRequest() {
        }

        public CreateProductRequest(String productId, String productName, String imageUrl, LocalDate expiryDate,
                                    String categoryId, String supplierId, BigDecimal importPrice, Double vat,
                                    BigDecimal sellingPrice, String unit, Integer quantity) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.expiryDate = expiryDate;
            this.categoryId = categoryId;
            this.supplierId = supplierId;
            this.importPrice = importPrice;
            this.vat = vat;
            this.sellingPrice = sellingPrice;
            this.unit = unit;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }

        public BigDecimal getImportPrice() {
            return importPrice;
        }

        public void setImportPrice(BigDecimal importPrice) {
            this.importPrice = importPrice;
        }

        public Double getVat() {
            return vat;
        }

        public void setVat(Double vat) {
            this.vat = vat;
        }

        public BigDecimal getSellingPrice() {
            return sellingPrice;
        }

        public void setSellingPrice(BigDecimal sellingPrice) {
            this.sellingPrice = sellingPrice;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateProductRequest {
        private String productName;
        private String imageUrl;
        private String categoryId;
        private String supplierId;
        private String unit;
        private Integer quantity;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        public UpdateProductRequest() {
        }

        public UpdateProductRequest(String productName, String imageUrl, String categoryId,
                                    String supplierId, String unit, LocalDate expiryDate) {
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.categoryId = categoryId;
            this.supplierId = supplierId;
            this.unit = unit;
            this.expiryDate = expiryDate;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getStock() {
            return quantity;
        }

        public void setStock(Integer stock) {
            this.quantity = stock;
        }
    }
}
