package com.mycompany.bachhoaxanhonline.module.category;

import com.mycompany.bachhoaxanhonline.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class CategoryRequest {

    public static class CreateCategoryRequest {

        private String categoryId;

        @NotBlank(message = "Tên loại sản phẩm không được để trống")
        private String categoryName;

        @NotNull(message = "Phần trăm lợi nhuận không được để trống")
        @PositiveOrZero(message = "Phần trăm lợi nhuận phải lớn hơn hoặc bằng 0")
        private Double profitMargin;

        public CreateCategoryRequest() {
        }

        public CreateCategoryRequest(String categoryId, String categoryName, Double profitMargin) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.profitMargin = profitMargin;
        }

        public CreateCategoryRequest(String categoryName, Double profitMargin) {
            this.categoryName = categoryName;
            this.profitMargin = profitMargin;
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
    }

    public static class UpdateCategoryRequest {

        @NotBlank(message = "Tên loại sản phẩm không được để trống")
        private String categoryName;

        @NotNull(message = "Phần trăm lợi nhuận không được để trống")
        @PositiveOrZero(message = "Phần trăm lợi nhuận phải lớn hơn hoặc bằng 0")
        private Double profitMargin;

        public UpdateCategoryRequest() {
        }

        public UpdateCategoryRequest(String categoryName, Double profitMargin) {
            this.categoryName = categoryName;
            this.profitMargin = profitMargin;
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
    }
}
