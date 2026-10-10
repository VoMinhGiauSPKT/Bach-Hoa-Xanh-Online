package com.mycompany.bachhoaxanhonline.module.review;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService() {
        this.reviewRepository = new ReviewRepository();
    }

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public static class ReviewException extends RuntimeException {
        private final int statusCode;

        public ReviewException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * 1. GET /review/product/:productId (Công khai)
     */
    public ApiResponse<ReviewResponse.ProductReviewsData> getProductReviews(
            String productId, Integer rating, int page, int limit) {

        if (productId == null || productId.trim().isEmpty()) {
            throw new ReviewException(400, "Mã sản phẩm không được để trống");
        }

        if (!reviewRepository.existsActiveProduct(productId)) {
            throw new ReviewException(404, "Sản phẩm không tồn tại hoặc đã bị xóa mềm");
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new ReviewException(400, "Số sao không hợp lệ (phải từ 1 đến 5 sao)");
        }

        if (page < 1) page = 1;
        if (limit < 1) limit = 5;
        int offset = (page - 1) * limit;

        ReviewResponse.ReviewSummary summary = reviewRepository.getProductReviewSummary(productId);
        List<ReviewResponse.ProductReviewItem> reviews = reviewRepository.findProductReviews(productId, rating, limit, offset);

        ReviewResponse.ProductReviewsData data = new ReviewResponse.ProductReviewsData(summary, page, limit, reviews);
        return new ApiResponse<>(200, "Lấy danh sách đánh giá thành công", data);
    }

    /**
     * 2. POST /review (Khách hàng)
     */
    public ApiResponse<ReviewResponse.CreateReviewData> createReview(
            String customerId, ReviewRequest.CreateReviewRequest request) {

        if (request == null) {
            throw new ReviewException(400, "Thiếu thông tin đánh giá");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new ReviewException(400, "Thiếu thông tin bắt buộc: " + violation);
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ReviewException(400, "Số sao không nằm trong khoảng từ 1 đến 5");
        }

        if (!reviewRepository.existsActiveProduct(request.getProductId())) {
            throw new ReviewException(404, "Sản phẩm không tồn tại hoặc đã bị xóa mềm");
        }

        // Kiểm tra đã mua và thanh toán đơn hàng thành công chưa
        if (!reviewRepository.hasPaidOrderWithProduct(customerId, request.getProductId())) {
            throw new ReviewException(403, "Khách hàng chưa mua sản phẩm này hoặc đơn hàng chưa hoàn tất thanh toán (trạng thái khác DATHANHTOAN)");
        }

        // Kiểm tra đã đánh giá sản phẩm trước đó chưa
        if (reviewRepository.hasReviewed(customerId, request.getProductId())) {
            throw new ReviewException(409, "Bạn đã đánh giá sản phẩm này trước đó rồi. Vui lòng dùng tính năng chỉnh sửa đánh giá.");
        }

        LocalDateTime now = LocalDateTime.now();
        Review review = new Review(customerId, request.getProductId(), request.getRating(), request.getComment(), now);
        Long reviewId = reviewRepository.createReview(review);

        ReviewResponse.CreateReviewData data = new ReviewResponse.CreateReviewData(
                reviewId,
                request.getProductId(),
                request.getRating(),
                request.getComment(),
                ReviewRepository.formatIso8601(now)
        );

        return new ApiResponse<>(201, "Đánh giá sản phẩm thành công", data);
    }

    /**
     * 3. PUT /review/:id (Khách hàng chính chủ)
     */
    public ApiResponse<ReviewResponse.UpdateReviewData> updateReview(
            String customerId, Long reviewId, ReviewRequest.UpdateReviewRequest request) {

        if (request == null) {
            throw new ReviewException(400, "Thiếu thông tin cập nhật");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new ReviewException(400, "Thiếu thông tin bắt buộc: " + violation);
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ReviewException(400, "Số sao không hợp lệ (phải từ 1 đến 5 sao)");
        }

        Optional<Review> optReview = reviewRepository.findById(reviewId);
        if (optReview.isEmpty() || Boolean.TRUE.equals(optReview.get().getDeleted())) {
            throw new ReviewException(404, "Không tìm thấy đánh giá với mã ID tương ứng hoặc bài đánh giá đã bị xóa");
        }

        Review review = optReview.get();
        if (!customerId.equals(review.getMaKhachHang())) {
            throw new ReviewException(403, "Đánh giá này không thuộc quyền sở hữu của khách hàng đang đăng nhập");
        }

        reviewRepository.updateReview(reviewId, request.getRating(), request.getComment());

        LocalDateTime updatedAt = LocalDateTime.now();
        ReviewResponse.UpdateReviewData data = new ReviewResponse.UpdateReviewData(
                reviewId,
                review.getMaSanPham(),
                request.getRating(),
                request.getComment(),
                ReviewRepository.formatIso8601(updatedAt)
        );

        return new ApiResponse<>(200, "Cập nhật đánh giá thành công", data);
    }

    /**
     * 4. DELETE /review/:id (Khách hàng chính chủ hoặc STAFF / ADMIN)
     */
    public ApiResponse<Void> deleteReview(String userId, String userRole, Long reviewId) {
        Optional<Review> optReview = reviewRepository.findById(reviewId);
        if (optReview.isEmpty() || Boolean.TRUE.equals(optReview.get().getDeleted())) {
            throw new ReviewException(404, "Không tìm thấy đánh giá cần xóa");
        }

        Review review = optReview.get();
        boolean isStaffOrAdmin = "ADMIN".equalsIgnoreCase(userRole) || "STAFF".equalsIgnoreCase(userRole);

        if (!isStaffOrAdmin) {
            // Là CUSTOMER thì chỉ được xóa bài của chính mình
            if (!userId.equals(review.getMaKhachHang())) {
                throw new ReviewException(403, "Khách hàng cố tình xóa đánh giá của người khác (phải ADMIN/STAFF mới được quyền xóa)");
            }
        }

        reviewRepository.softDeleteReview(reviewId);
        return new ApiResponse<>(200, "Xóa đánh giá thành công");
    }

    /**
     * 5. GET /review/me (Khách hàng xem các bài đánh giá của chính mình)
     */
    public ApiResponse<ReviewResponse.MyReviewsData> getMyReviews(
            String customerId, Integer rating, int page, int limit) {

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new ReviewException(400, "Số sao không hợp lệ (phải từ 1 đến 5 sao)");
        }

        if (page < 1) page = 1;
        if (limit < 1) limit = 10;
        int offset = (page - 1) * limit;

        long total = reviewRepository.countCustomerReviews(customerId, rating);
        List<ReviewResponse.MyReviewItem> reviews = reviewRepository.findCustomerReviews(customerId, rating, limit, offset);

        ReviewResponse.MyReviewsData data = new ReviewResponse.MyReviewsData(total, page, limit, reviews);
        return new ApiResponse<>(200, "Lấy danh sách đánh giá của tôi thành công", data);
    }

    /**
     * 6. GET /review (Quản trị viên / Nhân viên xem toàn bộ đánh giá)
     */
    public ApiResponse<ReviewResponse.AdminReviewsData> getAdminReviews(
            Boolean isDeleted, String productId, String customerId, Integer rating, String keyword, int page, int limit) {

        if (isDeleted == null) {
            isDeleted = Boolean.FALSE; // Mặc định false (chỉ lấy bài đang hiển thị)
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new ReviewException(400, "Số sao không hợp lệ (phải từ 1 đến 5 sao)");
        }

        if (page < 1) page = 1;
        if (limit < 1) limit = 20;
        int offset = (page - 1) * limit;

        long total = reviewRepository.countAdminReviews(isDeleted, productId, customerId, rating, keyword);
        List<ReviewResponse.AdminReviewItem> reviews = reviewRepository.findAdminReviews(isDeleted, productId, customerId, rating, keyword, limit, offset);

        ReviewResponse.AdminReviewsData data = new ReviewResponse.AdminReviewsData(total, page, limit, reviews);
        return new ApiResponse<>(200, "Lấy danh sách đánh giá quản trị thành công", data);
    }

    /**
     * 7. POST /review/:id/reply (Nhân viên / Quản trị viên phản hồi đánh giá)
     */
    public ApiResponse<Void> replyReview(String userId, String userRole, Long reviewId, String replyText) {
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"STAFF".equalsIgnoreCase(userRole)) {
            throw new ReviewException(403, "Chỉ nhân viên hoặc quản trị viên mới được quyền phản hồi đánh giá");
        }
        if (replyText == null || replyText.trim().isEmpty()) {
            throw new ReviewException(400, "Nội dung phản hồi không được để trống");
        }

        Optional<Review> opt = reviewRepository.findById(reviewId);
        if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getDeleted())) {
            throw new ReviewException(404, "Không tìm thấy đánh giá");
        }

        reviewRepository.saveReply(reviewId, replyText.trim(), LocalDateTime.now());
        return new ApiResponse<>(200, "Phản hồi đánh giá thành công");
    }
}
