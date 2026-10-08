package com.mycompany.bachhoaxanhonline.module.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewRequest {

    public static class CreateReviewRequest {

        @NotBlank(message = "Mã sản phẩm không được để trống")
        private String productId;

        @NotNull(message = "Số sao không được để trống")
        @Min(value = 1, message = "Số sao phải từ 1 đến 5")
        @Max(value = 5, message = "Số sao phải từ 1 đến 5")
        private Integer rating;

        private String comment;

        public CreateReviewRequest() {
        }

        public CreateReviewRequest(String productId, Integer rating, String comment) {
            this.productId = productId;
            this.rating = rating;
            this.comment = comment;
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
    }

    public static class UpdateReviewRequest {

        @NotNull(message = "Số sao không được để trống")
        @Min(value = 1, message = "Số sao phải từ 1 đến 5")
        @Max(value = 5, message = "Số sao phải từ 1 đến 5")
        private Integer rating;

        private String comment;

        public UpdateReviewRequest() {
        }

        public UpdateReviewRequest(Integer rating, String comment) {
            this.rating = rating;
            this.comment = comment;
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
    }
}
