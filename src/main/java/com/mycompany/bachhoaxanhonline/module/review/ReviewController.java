package com.mycompany.bachhoaxanhonline.module.review;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.middleware.SecurityContext;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ReviewController", urlPatterns = {"/review", "/review/*"})
public class ReviewController extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        try {
            if (pathInfo.startsWith("/product/")) {
                // 1. GET /review/product/:productId (Công khai)
                handleGetProductReviews(req, resp, pathInfo.substring("/product/".length()));
            } else if ("/me".equals(pathInfo)) {
                // 5. GET /review/me (Khách hàng)
                handleGetMyReviews(req, resp);
            } else if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
                // 6. GET /review (Admin / Staff)
                handleGetAdminReviews(req, resp);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /review" + pathInfo));
            }
        } catch (ReviewService.ReviewException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        try {
            if (pathInfo.matches("/\\d+/reply")) {
                Long reviewId = extractIdFromPath(pathInfo.substring(0, pathInfo.lastIndexOf("/reply")));
                handleReplyReview(req, resp, reviewId);
            } else if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
                // 2. POST /review (Khách hàng)
                handleCreateReview(req, resp);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /review" + pathInfo));
            }
        } catch (ReviewService.ReviewException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        try {
            Long reviewId = extractIdFromPath(pathInfo);
            if (reviewId != null) {
                // 3. PUT /review/:id (Khách hàng chính chủ)
                handleUpdateReview(req, resp, reviewId);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /review" + pathInfo));
            }
        } catch (ReviewService.ReviewException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        try {
            Long reviewId = extractIdFromPath(pathInfo);
            if (reviewId != null) {
                // 4. DELETE /review/:id (Khách hàng chính chủ hoặc Staff/Admin)
                handleDeleteReview(req, resp, reviewId);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /review" + pathInfo));
            }
        } catch (ReviewService.ReviewException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    // --- Xử lý chi tiết các endpoint ---

    private void handleGetProductReviews(HttpServletRequest req, HttpServletResponse resp, String productId) throws IOException {
        int page = parseInt(req.getParameter("page"), 1);
        int limit = parseInt(req.getParameter("limit"), 5);
        Integer rating = parseNullableInt(req.getParameter("rating"));

        ApiResponse<ReviewResponse.ProductReviewsData> response =
                reviewService.getProductReviews(productId, rating, page, limit);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleCreateReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = SecurityContext.getUserId(req);
        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        ReviewRequest.CreateReviewRequest request = JsonUtil.fromJson(req.getReader(), ReviewRequest.CreateReviewRequest.class);
        ApiResponse<ReviewResponse.CreateReviewData> response = reviewService.createReview(userId, request);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
    }

    private void handleUpdateReview(HttpServletRequest req, HttpServletResponse resp, Long reviewId) throws IOException {
        String userId = SecurityContext.getUserId(req);
        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        ReviewRequest.UpdateReviewRequest request = JsonUtil.fromJson(req.getReader(), ReviewRequest.UpdateReviewRequest.class);
        ApiResponse<ReviewResponse.UpdateReviewData> response = reviewService.updateReview(userId, reviewId, request);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleDeleteReview(HttpServletRequest req, HttpServletResponse resp, Long reviewId) throws IOException {
        String userId = SecurityContext.getUserId(req);
        String userRole = SecurityContext.getUserRole(req);

        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        ApiResponse<Void> response = reviewService.deleteReview(userId, userRole, reviewId);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleGetMyReviews(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = SecurityContext.getUserId(req);
        String userRole = SecurityContext.getUserRole(req);

        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        if (userRole != null && ("ADMIN".equalsIgnoreCase(userRole) || "STAFF".equalsIgnoreCase(userRole))) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                    new ApiResponse<>(HttpServletResponse.SC_FORBIDDEN, "Tài khoản không phải là khách hàng (CUSTOMER)"));
            return;
        }

        int page = parseInt(req.getParameter("page"), 1);
        int limit = parseInt(req.getParameter("limit"), 10);
        Integer rating = parseNullableInt(req.getParameter("rating"));

        ApiResponse<ReviewResponse.MyReviewsData> response = reviewService.getMyReviews(userId, rating, page, limit);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleGetAdminReviews(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userRole = SecurityContext.getUserRole(req);

        // Chỉ cho phép STAFF hoặc ADMIN
        if (userRole == null || (!"ADMIN".equalsIgnoreCase(userRole) && !"STAFF".equalsIgnoreCase(userRole))) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                    new ApiResponse<>(HttpServletResponse.SC_FORBIDDEN, "Khách hàng (CUSTOMER) cố tình truy cập vào trang kiểm duyệt nội bộ"));
            return;
        }

        int page = parseInt(req.getParameter("page"), 1);
        int limit = parseInt(req.getParameter("limit"), 20);
        String productId = req.getParameter("productId");
        String customerId = req.getParameter("customerId");
        Integer rating = parseNullableInt(req.getParameter("rating"));
        String keyword = req.getParameter("keyword");
        Boolean isDeleted = parseNullableBoolean(req.getParameter("isDeleted"));

        ApiResponse<ReviewResponse.AdminReviewsData> response =
                reviewService.getAdminReviews(isDeleted, productId, customerId, rating, keyword, page, limit);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleReplyReview(HttpServletRequest req, HttpServletResponse resp, Long reviewId) throws IOException {
        String userId = SecurityContext.getUserId(req);
        String userRole = SecurityContext.getUserRole(req);

        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        java.util.Map<String, Object> body = JsonUtil.fromJson(req.getReader(), java.util.Map.class);
        String reply = body != null ? (String) body.get("reply") : null;

        ApiResponse<Void> response = reviewService.replyReview(userId, userRole, reviewId, reply);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    // --- Helper trích xuất và ép kiểu ---

    private Long extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty()) {
            return null;
        }
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        try {
            return Long.valueOf(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String val, int defaultVal) {
        if (val == null || val.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Integer parseNullableInt(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseNullableBoolean(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        return Boolean.valueOf(val.trim());
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
