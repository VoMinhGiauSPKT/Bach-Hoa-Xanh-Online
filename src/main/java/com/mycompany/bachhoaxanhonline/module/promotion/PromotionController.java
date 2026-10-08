package com.mycompany.bachhoaxanhonline.module.promotion;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "PromotionController", urlPatterns = {"/promotion", "/promotion/*"})
public class PromotionController extends HttpServlet {

    private final PromotionService promotionService = new PromotionService();

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
        resp.setHeader("Access-Control-Max-Age", "3600");
    }

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
            if ("/available".equalsIgnoreCase(pathInfo)) {
                // 1. GET /promotion/available
                String totalAmountStr = req.getParameter("totalAmount");
                if (totalAmountStr == null || totalAmountStr.trim().isEmpty()) {
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                            new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "totalAmount không hợp lệ."));
                    return;
                }
                double totalAmount;
                try {
                    totalAmount = Double.parseDouble(totalAmountStr.trim());
                } catch (NumberFormatException e) {
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                            new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "totalAmount không hợp lệ."));
                    return;
                }

                PromotionResponse.ApiResponse<?> result = promotionService.getAvailablePromotions(totalAmount);
                JsonUtil.sendJsonResponse(resp, result.getStatus(), result);
            } else if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
                // 2. GET /promotion (Admin)
                String keyword = req.getParameter("keyword");
                String type = req.getParameter("type");
                Integer page = null;
                Integer limit = null;

                if (req.getParameter("page") != null) {
                    try {
                        page = Integer.parseInt(req.getParameter("page").trim());
                    } catch (NumberFormatException ignored) {}
                }
                if (req.getParameter("limit") != null) {
                    try {
                        limit = Integer.parseInt(req.getParameter("limit").trim());
                    } catch (NumberFormatException ignored) {}
                }

                PromotionResponse.ApiResponse<?> result = promotionService.getAdminPromotions(keyword, type, page, limit);
                JsonUtil.sendJsonResponse(resp, result.getStatus(), result);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /promotion" + pathInfo));
            }
        } catch (PromotionService.PromotionException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new PromotionResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
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
            if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
                // 3. POST /promotion (Admin tạo mới)
                PromotionRequest.CreatePromotionRequest reqBody =
                        JsonUtil.fromJson(req.getReader(), PromotionRequest.CreatePromotionRequest.class);

                PromotionResponse.ApiResponse<?> result = promotionService.createPromotion(reqBody);
                JsonUtil.sendJsonResponse(resp, result.getStatus(), result);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /promotion" + pathInfo));
            }
        } catch (PromotionService.PromotionException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new PromotionResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã khuyến mãi trên URL"));
            return;
        }

        try {
            // 4. PUT /promotion/:code
            String code = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            if (code.contains("/")) {
                code = code.substring(0, code.indexOf("/"));
            }

            PromotionRequest.UpdatePromotionRequest reqBody =
                    JsonUtil.fromJson(req.getReader(), PromotionRequest.UpdatePromotionRequest.class);


            PromotionResponse.ApiResponse<?> result = promotionService.updatePromotion(code, reqBody);
            JsonUtil.sendJsonResponse(resp, result.getStatus(), result);
        } catch (PromotionService.PromotionException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new PromotionResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã khuyến mãi trên URL"));
            return;
        }

        try {
            // 5. DELETE /promotion/:code
            String code = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            if (code.contains("/")) {
                code = code.substring(0, code.indexOf("/"));
            }

            PromotionResponse.ApiResponse<?> result = promotionService.deletePromotion(code);
            JsonUtil.sendJsonResponse(resp, result.getStatus(), result);
        } catch (PromotionService.PromotionException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new PromotionResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new PromotionResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }
}
