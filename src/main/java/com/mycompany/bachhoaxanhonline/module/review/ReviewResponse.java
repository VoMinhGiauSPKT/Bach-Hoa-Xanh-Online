package com.mycompany.bachhoaxanhonline.module.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

public class ReviewResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiResponse<T> {
        private int status;
        private String message;
        private T data;

        public ApiResponse() {
        }

        public ApiResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public ApiResponse(int status, String message, T data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    // --- 1. GET /review/product/:productId DTOs ---

    public static class ReviewSummary {
        private double averageRating;
        private long totalReviews;
        private Map<String, Long> starCounts;

        public ReviewSummary() {
        }

        public ReviewSummary(double averageRating, long totalReviews, Map<String, Long> starCounts) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.starCounts = starCounts;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public long getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(long totalReviews) {
            this.totalReviews = totalReviews;
        }

        public Map<String, Long> getStarCounts() {
            return starCounts;
        }

        public void setStarCounts(Map<String, Long> starCounts) {
            this.starCounts = starCounts;
        }
    }

    public static class CustomerPublicInfo {
        private String fullName;
        private String username;

        public CustomerPublicInfo() {
        }

        public CustomerPublicInfo(String fullName, String username) {
            this.fullName = fullName;
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class ProductReviewItem {
        private Long reviewId;
        private Integer rating;
        private String comment;
        private String createdAt;
        private CustomerPublicInfo customer;

        public ProductReviewItem() {
        }

        public ProductReviewItem(Long reviewId, Integer rating, String comment, String createdAt, CustomerPublicInfo customer) {
            this.reviewId = reviewId;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
            this.customer = customer;
        }

        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public CustomerPublicInfo getCustomer() {
            return customer;
        }

        public void setCustomer(CustomerPublicInfo customer) {
            this.customer = customer;
        }
    }

    public static class ProductReviewsData {
        private ReviewSummary summary;
        private int page;
        private int limit;
        private List<ProductReviewItem> reviews;

        public ProductReviewsData() {
        }

        public ProductReviewsData(ReviewSummary summary, int page, int limit, List<ProductReviewItem> reviews) {
            this.summary = summary;
            this.page = page;
            this.limit = limit;
            this.reviews = reviews;
        }

        public ReviewSummary getSummary() {
            return summary;
        }

        public void setSummary(ReviewSummary summary) {
            this.summary = summary;
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

        public List<ProductReviewItem> getReviews() {
            return reviews;
        }

        public void setReviews(List<ProductReviewItem> reviews) {
            this.reviews = reviews;
        }
    }

    // --- 2. POST /review DTO ---

    public static class CreateReviewData {
        private Long reviewId;
        private String productId;
        private Integer rating;
        private String comment;
        private String createdAt;

        public CreateReviewData() {
        }

        public CreateReviewData(Long reviewId, String productId, Integer rating, String comment, String createdAt) {
            this.reviewId = reviewId;
            this.productId = productId;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
        }

        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    // --- 3. PUT /review/:id DTO ---

    public static class UpdateReviewData {
        private Long reviewId;
        private String productId;
        private Integer rating;
        private String comment;
        private String updatedAt;

        public UpdateReviewData() {
        }

        public UpdateReviewData(Long reviewId, String productId, Integer rating, String comment, String updatedAt) {
            this.reviewId = reviewId;
            this.productId = productId;
            this.rating = rating;
            this.comment = comment;
            this.updatedAt = updatedAt;
        }

        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    // --- 4. GET /review/me DTOs ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductDetailInfo {
        private String productId;
        private String productName;
        private String imageUrl;
        private String unit;

        public ProductDetailInfo() {
        }

        public ProductDetailInfo(String productId, String productName, String imageUrl, String unit) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.unit = unit;
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
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MyReviewItem {
        private Long reviewId;
        private Integer rating;
        private String comment;
        private String createdAt;
        private ProductDetailInfo product;

        public MyReviewItem() {
        }

        public MyReviewItem(Long reviewId, Integer rating, String comment, String createdAt, ProductDetailInfo product) {
            this.reviewId = reviewId;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
            this.product = product;
        }

        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public ProductDetailInfo getProduct() {
            return product;
        }

        public void setProduct(ProductDetailInfo product) {
            this.product = product;
        }
    }

    public static class MyReviewsData {
        private long total;
        private int page;
        private int limit;
        private List<MyReviewItem> reviews;

        public MyReviewsData() {
        }

        public MyReviewsData(long total, int page, int limit, List<MyReviewItem> reviews) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.reviews = reviews;
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

        public List<MyReviewItem> getReviews() {
            return reviews;
        }

        public void setReviews(List<MyReviewItem> reviews) {
            this.reviews = reviews;
        }
    }

    // --- 5. GET /review (Admin / Staff) DTOs ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdminCustomerInfo {
        private String customerId;
        private String fullName;
        private String username;
        private String phoneNumber;

        public AdminCustomerInfo() {
        }

        public AdminCustomerInfo(String customerId, String fullName, String username, String phoneNumber) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.username = username;
            this.phoneNumber = phoneNumber;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdminProductInfo {
        private String productId;
        private String productName;
        private String imageUrl;

        public AdminProductInfo() {
        }

        public AdminProductInfo(String productId, String productName, String imageUrl) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
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
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdminReviewItem {
        private Long reviewId;
        private Integer rating;
        private String comment;
        private String createdAt;
        private Boolean isDeleted;
        private AdminCustomerInfo customer;
        private AdminProductInfo product;

        public AdminReviewItem() {
        }

        public AdminReviewItem(Long reviewId, Integer rating, String comment, String createdAt, Boolean isDeleted, AdminCustomerInfo customer, AdminProductInfo product) {
            this.reviewId = reviewId;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
            this.isDeleted = isDeleted;
            this.customer = customer;
            this.product = product;
        }

        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public Boolean getIsDeleted() {
            return isDeleted;
        }

        public void setIsDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
        }

        public AdminCustomerInfo getCustomer() {
            return customer;
        }

        public void setCustomer(AdminCustomerInfo customer) {
            this.customer = customer;
        }

        public AdminProductInfo getProduct() {
            return product;
        }

        public void setProduct(AdminProductInfo product) {
            this.product = product;
        }
    }

    public static class AdminReviewsData {
        private long total;
        private int page;
        private int limit;
        private List<AdminReviewItem> reviews;

        public AdminReviewsData() {
        }

        public AdminReviewsData(long total, int page, int limit, List<AdminReviewItem> reviews) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.reviews = reviews;
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

        public List<AdminReviewItem> getReviews() {
            return reviews;
        }

        public void setReviews(List<AdminReviewItem> reviews) {
            this.reviews = reviews;
        }
    }
}
