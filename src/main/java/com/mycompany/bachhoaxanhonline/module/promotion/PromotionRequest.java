package com.mycompany.bachhoaxanhonline.module.promotion;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class PromotionRequest {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreatePromotionRequest {
        private String promotionCode;
        private String promotionName;
        private String description;
        private String discountType;
        private Double discountValue;
        private Double maxDiscount;
        private Double minOrderAmount;
        private Integer usageLimit;
        private String startDate;
        private String endDate;

        public CreatePromotionRequest() {
        }

        public CreatePromotionRequest(String promotionCode, String promotionName, String description,
                                      String discountType, Double discountValue, Double maxDiscount,
                                      Double minOrderAmount, Integer usageLimit, String startDate, String endDate) {
            this.promotionCode = promotionCode;
            this.promotionName = promotionName;
            this.description = description;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.maxDiscount = maxDiscount;
            this.minOrderAmount = minOrderAmount;
            this.usageLimit = usageLimit;
            this.startDate = startDate;
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

        public Double getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(Double discountValue) {
            this.discountValue = discountValue;
        }

        public Double getMaxDiscount() {
            return maxDiscount;
        }

        public void setMaxDiscount(Double maxDiscount) {
            this.maxDiscount = maxDiscount;
        }

        public Double getMinOrderAmount() {
            return minOrderAmount;
        }

        public void setMinOrderAmount(Double minOrderAmount) {
            this.minOrderAmount = minOrderAmount;
        }

        public Integer getUsageLimit() {
            return usageLimit;
        }

        public void setUsageLimit(Integer usageLimit) {
            this.usageLimit = usageLimit;
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdatePromotionRequest {
        private String promotionName;
        private String description;
        private Double maxDiscount;
        private Integer remainingUsage;
        private String endDate;

        public UpdatePromotionRequest() {
        }

        public UpdatePromotionRequest(String promotionName, String description, Integer remainingUsage, String endDate) {
            this.promotionName = promotionName;
            this.description = description;
            this.remainingUsage = remainingUsage;
            this.endDate = endDate;
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

        public Double getMaxDiscount() {
            return maxDiscount;
        }

        public void setMaxDiscount(Double maxDiscount) {
            this.maxDiscount = maxDiscount;
        }

        public Integer getRemainingUsage() {
            return remainingUsage;
        }

        public void setRemainingUsage(Integer remainingUsage) {
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
