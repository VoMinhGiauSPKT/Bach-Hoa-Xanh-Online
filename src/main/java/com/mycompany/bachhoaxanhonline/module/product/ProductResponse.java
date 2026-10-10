package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductResponse {

    public static class CategoryInfo {
        private String categoryId;
        private String categoryName;

        public CategoryInfo() {
        }

        public CategoryInfo(String categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }

    public static class SupplierInfo {
        private String supplierId;
        private String supplierName;

        public SupplierInfo() {
        }

        public SupplierInfo(String supplierId, String supplierName) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductDetailData {
        private String productId;
        private String productName;
        private String imageUrl;
        private CategoryInfo category;
        private SupplierInfo supplier;
        private BigDecimal price;
        private Double vat;
        private String unit;
        private Integer stock;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        private Double ratingAverage;
        private Long totalReviews;

        public ProductDetailData() {
        }

        public ProductDetailData(String productId, String productName, String imageUrl,
                                 CategoryInfo category, SupplierInfo supplier,
                                 BigDecimal price, Double vat, String unit, Integer stock,
                                 LocalDate expiryDate, Double ratingAverage, Long totalReviews) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.category = category;
            this.supplier = supplier;
            this.price = price;
            this.vat = vat;
            this.unit = unit;
            this.stock = stock;
            this.expiryDate = expiryDate;
            this.ratingAverage = ratingAverage;
            this.totalReviews = totalReviews;
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

        public CategoryInfo getCategory() {
            return category;
        }

        public void setCategory(CategoryInfo category) {
            this.category = category;
        }

        public SupplierInfo getSupplier() {
            return supplier;
        }

        public void setSupplier(SupplierInfo supplier) {
            this.supplier = supplier;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Double getVat() {
            return vat;
        }

        public void setVat(Double vat) {
            this.vat = vat;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }

        public Double getRatingAverage() {
            return ratingAverage;
        }

        public void setRatingAverage(Double ratingAverage) {
            this.ratingAverage = ratingAverage;
        }

        public Long getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(Long totalReviews) {
            this.totalReviews = totalReviews;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductItem {
        private String productId;
        private String productName;
        private String imageUrl;
        private CategoryInfo category;
        private BigDecimal price;
        private String unit;
        private Integer stock;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        public ProductItem() {
        }

        public ProductItem(String productId, String productName, String imageUrl, CategoryInfo category,
                           BigDecimal price, String unit, Integer stock, LocalDate expiryDate) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.category = category;
            this.price = price;
            this.unit = unit;
            this.stock = stock;
            this.expiryDate = expiryDate;
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

        public CategoryInfo getCategory() {
            return category;
        }

        public void setCategory(CategoryInfo category) {
            this.category = category;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }
    }

    public static class ProductListData {
        private long total;
        private int page;
        private int limit;
        private List<ProductItem> products;

        public ProductListData() {
        }

        public ProductListData(long total, int page, int limit, List<ProductItem> products) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.products = products;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public List<ProductItem> getProducts() {
            return products;
        }

        public void setProducts(List<ProductItem> products) {
            this.products = products;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateProductData {
        private String productId;
        private String productName;
        private BigDecimal sellingPrice;
        private Integer quantity;
        private String unit;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        public CreateProductData() {
        }

        public CreateProductData(String productId, String productName, BigDecimal sellingPrice,
                                 Integer quantity, String unit, LocalDate expiryDate) {
            this.productId = productId;
            this.productName = productName;
            this.sellingPrice = sellingPrice;
            this.quantity = quantity;
            this.unit = unit;
            this.expiryDate = expiryDate;
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

        public BigDecimal getSellingPrice() {
            return sellingPrice;
        }

        public void setSellingPrice(BigDecimal sellingPrice) {
            this.sellingPrice = sellingPrice;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
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
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateProductData {
        private String productId;
        private String productName;
        private String imageUrl;
        private String unit;
        private Integer stock;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        public UpdateProductData() {
        }

        public UpdateProductData(String productId, String productName, String imageUrl,
                                 String unit, LocalDate expiryDate) {
            this(productId, productName, imageUrl, unit, expiryDate, null);
        }

        public UpdateProductData(String productId, String productName, String imageUrl,
                                 String unit, LocalDate expiryDate, Integer stock) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.unit = unit;
            this.expiryDate = expiryDate;
            this.stock = stock;
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

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }
    }
}
