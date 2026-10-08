package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ProductController", urlPatterns = {"/product", "/product/*"})
public class ProductController extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String pathInfo = req.getPathInfo();
        String productId = extractIdFromPath(pathInfo);

        try {
            if (productId == null) {
                // 1. GET /product -> Danh sách sản phẩm có phân trang, lọc, sắp xếp
                String pageStr = req.getParameter("page");
                String limitStr = req.getParameter("limit");
                String keyword = req.getParameter("keyword");
                String categoryId = req.getParameter("categoryId");
                String sortBy = req.getParameter("sortBy");
                String inStockStr = req.getParameter("inStock");

                ApiResponse<ProductResponse.ProductListData> response =
                        productService.getProducts(pageStr, limitStr, keyword, categoryId, sortBy, inStockStr);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } else {
                // 2. GET /product/{id} -> Chi tiết sản phẩm kèm đánh giá
                ApiResponse<ProductResponse.ProductDetailData> response =
                        productService.getProductDetail(productId);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            }
        } catch (ProductService.ProductException e) {
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

        // Kiểm tra quyền STAFF hoặc ADMIN
        String authError = authenticateRole(req, "ADMIN", "STAFF");
        if (authError != null) {
            int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
            JsonUtil.sendJsonResponse(resp, statusCode, new ApiResponse<>(statusCode, authError));
            return;
        }

        try {
            ProductRequest.CreateProductRequest request = JsonUtil.fromJson(
                    req.getReader(), ProductRequest.CreateProductRequest.class);
            ApiResponse<ProductResponse.CreateProductData> response =
                    productService.createProduct(request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
        } catch (ProductService.ProductException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException | java.time.format.DateTimeParseException e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu yêu cầu sai định dạng: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        // Kiểm tra quyền STAFF hoặc ADMIN
        String authError = authenticateRole(req, "ADMIN", "STAFF");
        if (authError != null) {
            int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
            JsonUtil.sendJsonResponse(resp, statusCode, new ApiResponse<>(statusCode, authError));
            return;
        }

        String pathInfo = req.getPathInfo();
        String productId = extractIdFromPath(pathInfo);
        if (productId == null || productId.isEmpty()) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã sản phẩm trên đường dẫn"));
            return;
        }

        try {
            ProductRequest.UpdateProductRequest request = JsonUtil.fromJson(
                    req.getReader(), ProductRequest.UpdateProductRequest.class);
            ApiResponse<ProductResponse.UpdateProductData> response =
                    productService.updateProduct(productId, request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (ProductService.ProductException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException | java.time.format.DateTimeParseException e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu yêu cầu sai định dạng: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        // Kiểm tra quyền STAFF hoặc ADMIN
        String authError = authenticateRole(req, "ADMIN", "STAFF");
        if (authError != null) {
            int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
            JsonUtil.sendJsonResponse(resp, statusCode, new ApiResponse<>(statusCode, authError));
            return;
        }

        String pathInfo = req.getPathInfo();
        String productId = extractIdFromPath(pathInfo);
        if (productId == null || productId.isEmpty()) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã sản phẩm trên đường dẫn"));
            return;
        }

        try {
            ApiResponse<Void> response = productService.deleteProduct(productId);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (ProductService.ProductException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private String extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) {
            return null;
        }
        String cleaned = pathInfo.trim();
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String authenticateRole(HttpServletRequest req, String... allowedRoles) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Chưa đăng nhập hoặc token không hợp lệ / hết hạn";
        }

        String token = authHeader.substring(7).trim();
        if (!JwtUtil.validateToken(token)) {
            return "Chưa đăng nhập hoặc token không hợp lệ / hết hạn";
        }

        String role = JwtUtil.getRoleFromToken(token);
        if (role == null) {
            return "Người dùng không có quyền quản lý sản phẩm";
        }

        for (String allowedRole : allowedRoles) {
            if (allowedRole.equalsIgnoreCase(role)) {
                return null; // Hợp lệ
            }
        }

        return "Người dùng không có quyền quản lý sản phẩm";
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
