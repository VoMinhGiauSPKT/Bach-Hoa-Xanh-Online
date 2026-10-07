package com.mycompany.bachhoaxanhonline.module.promotion;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class PromotionResponse {

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

    // --- 1. GET /promotion/available DTO ---
    public static class AvailablePromotionItem {
        private String promotionCode;
        private String promotionName;
        private String description;
        private String discountType;
        private double discountValue;
        private double minOrderAmount;
        private Double maxDiscount;
        private double estimatedDiscount;
        private boolean isEligible;
        private String unmetReason;

        public AvailablePromotionItem() {
        }

        public AvailablePromotionItem(String promotionCode, String promotionName, String description,
                                      String discountType, double discountValue, double minOrderAmount,
                                      Double maxDiscount, double estimatedDiscount, boolean isEligible,
                                      String unmetReason) {
            this.promotionCode = promotionCode;
            this.promotionName = promotionName;
            this.description = description;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.minOrderAmount = minOrderAmount;
            this.maxDiscount = maxDiscount;
            this.estimatedDiscount = estimatedDiscount;
            this.isEligible = isEligible;
            this.unmetReason = unmetReason;
        }

        public String getPromotionCode() {
            return promotionCode;
        }

        public void setPromotionCode(String promotionCode) {
            this.promotionCode = promotionCode;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public void setPromotionName(String promotionName) {
            this.promotionName = promotionName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDiscountType() {
            return discountType;
        }

        public void setDiscountType(String discountType) {
            this.discountType = discountType;
        }

        public double getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(double discountValue) {
            this.discountValue = discountValue;
        }

        public double getMinOrderAmount() {
            return minOrderAmount;
        }

        public void setMinOrderAmount(double minOrderAmount) {
            this.minOrderAmount = minOrderAmount;
        }

        public Double getMaxDiscount() {
            return maxDiscount;
        }

        public void setMaxDiscount(Double maxDiscount) {
            this.maxDiscount = maxDiscount;
        }

        public double getEstimatedDiscount() {
            return estimatedDiscount;
        }

        public void setEstimatedDiscount(double estimatedDiscount) {
            this.estimatedDiscount = estimatedDiscount;
        }

        public boolean isEligible() {
            return isEligible;
        }

        public void setEligible(boolean eligible) {
            isEligible = eligible;
        }

        public String getUnmetReason() {
            return unmetReason;
        }

        public void setUnmetReason(String unmetReason) {
            this.unmetReason = unmetReason;
        }
    }

    // --- 2. GET /promotion (Admin) DTOs ---
    public static class AdminPromotionItem {
        private String promotionCode;
        private String promotionName;
        private String discountType;
        private double discountValue;
        private Double maxDiscount;
        private double minOrderAmount;
        private int remainingUsage;
        private String startDate;
        private String endDate;
        private boolean isDeleted;
        private String status;

        public AdminPromotionItem() {
        }

        public AdminPromotionItem(String promotionCode, String promotionName, String discountType,
                                  double discountValue, Double maxDiscount, double minOrderAmount,
                                  int remainingUsage, String startDate, String endDate,
                                  boolean isDeleted, String status) {
            this.promotionCode = promotionCode;
            this.promotionName = promotionName;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.maxDiscount = maxDiscount;
            this.minOrderAmount = minOrderAmount;
            this.remainingUsage = remainingUsage;
            this.startDate = startDate;
            this.endDate = endDate;
            this.isDeleted = isDeleted;
            this.status = status;
        }

        public String getPromotionCode() {
            return promotionCode;
        }

        public void setPromotionCode(String promotionCode) {
            this.promotionCode = promotionCode;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public void setPromotionName(String promotionName) {
            this.promotionName = promotionName;
        }

        public String getDiscountType() {
            return discountType;
        }

        public void setDiscountType(String discountType) {
            this.discountType = discountType;
        }

        public double getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(double discountValue) {
            this.discountValue = discountValue;
        }

        public Double getMaxDiscount() {
            return maxDiscount;
        }

        public void setMaxDiscount(Double maxDiscount) {
            this.maxDiscount = maxDiscount;
        }

        public double getMinOrderAmount() {
            return minOrderAmount;
        }

        public void setMinOrderAmount(double minOrderAmount) {
            this.minOrderAmount = minOrderAmount;
        }

        public int getRemainingUsage() {
            return remainingUsage;
        }

        public void setRemainingUsage(int remainingUsage) {
            this.remainingUsage = remainingUsage;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public boolean isIsDeleted() {
            return isDeleted;
        }

        public void setIsDeleted(boolean deleted) {
            isDeleted = deleted;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class AdminPromotionsData {
        private long total;
        private int page;
        private int limit;
        private List<AdminPromotionItem> promotions;

        public AdminPromotionsData() {
        }

        public AdminPromotionsData(long total, int page, int limit, List<AdminPromotionItem> promotions) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.promotions = promotions;
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

        public List<AdminPromotionItem> getPromotions() {
            return promotions;
        }

        public void setPromotions(List<AdminPromotionItem> promotions) {
            this.promotions = promotions;
        }
    }

    // --- 3. POST /promotion DTO ---
    public static class CreatePromotionData {
        private String promotionCode;
        private String promotionName;
        private String discountType;
        private double discountValue;
        private Double maxDiscount;
        private int remainingUsage;

        public CreatePromotionData() {
        }

        public CreatePromotionData(String promotionCode, String promotionName, String discountType,
                                   double discountValue, Double maxDiscount, int remainingUsage) {
            this.promotionCode = promotionCode;
            this.promotionName = promotionName;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.maxDiscount = maxDiscount;
            this.remainingUsage = remainingUsage;
        }

        public String getPromotionCode() {
            return promotionCode;
        }

        public void setPromotionCode(String promotionCode) {
            this.promotionCode = promotionCode;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public void setPromotionName(String promotionName) {
            this.promotionName = promotionName;
        }

        public String getDiscountType() {
            return discountType;
        }

        public void setDiscountType(String discountType) {
            this.discountType = discountType;
        }

        public double getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(double discountValue) {
            this.discountValue = discountValue;
        }

        public Double getMaxDiscount() {
            return maxDiscount;
        }

        public void setMaxDiscount(Double maxDiscount) {
            this.maxDiscount = maxDiscount;
        }

        public int getRemainingUsage() {
            return remainingUsage;
        }

        public void setRemainingUsage(int remainingUsage) {
            this.remainingUsage = remainingUsage;
        }
    }

    // --- 4. PUT /promotion/:code DTO ---
    public static class UpdatePromotionData {
        private String promotionCode;
        private String promotionName;
        private int remainingUsage;
        private String endDate;

        public UpdatePromotionData() {
        }

        public UpdatePromotionData(String promotionCode, String promotionName, int remainingUsage, String endDate) {
            this.promotionCode = promotionCode;
            this.promotionName = promotionName;
            this.remainingUsage = remainingUsage;
            this.endDate = endDate;
        }

        public String getPromotionCode() {
            return promotionCode;
        }

        public void setPromotionCode(String promotionCode) {
            this.promotionCode = promotionCode;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public void setPromotionName(String promotionName) {
            this.promotionName = promotionName;
        }

        public int getRemainingUsage() {
            return remainingUsage;
        }

        public void setRemainingUsage(int remainingUsage) {
            this.remainingUsage = remainingUsage;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}
