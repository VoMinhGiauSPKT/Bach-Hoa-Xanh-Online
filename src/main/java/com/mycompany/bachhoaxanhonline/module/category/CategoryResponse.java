package com.mycompany.bachhoaxanhonline.module.category;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CategoryResponse {

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

        public ApiResponse(int status, T data) {
            this.status = status;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryPublicItem {
        private String categoryId;
        private String categoryName;

        public CategoryPublicItem() {
        }

        public CategoryPublicItem(String categoryId, String categoryName) {
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryData {
        private String categoryId;
        private String categoryName;
        private Double profitMargin;
        private Boolean deleted;

        public CategoryData() {
        }

        public CategoryData(String categoryId, String categoryName, Double profitMargin, Boolean deleted) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.profitMargin = profitMargin;
            this.deleted = deleted;
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

        public Double getProfitMargin() {
            return profitMargin;
        }

        public void setProfitMargin(Double profitMargin) {
            this.profitMargin = profitMargin;
        }

        public Boolean getDeleted() {
            return deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }
    }
}
