package com.mycompany.bachhoaxanhonline.module.category;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "CategoryController", urlPatterns = {"/category", "/category/*"})
public class CategoryController extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                // 1. GET /category -> Lấy danh sách (Public)
                CategoryResponse.ApiResponse<List<CategoryResponse.CategoryPublicItem>> response =
                        categoryService.getPublicCategories();
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } else {
                // 2. GET /category/{id} -> Lấy chi tiết (ADMIN & STAFF only)
                String authError = authenticateRole(req, "ADMIN", "STAFF");
                if (authError != null) {
                    int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
                    JsonUtil.sendJsonResponse(resp, statusCode, new CategoryResponse.ApiResponse<>(statusCode, authError));
                    return;
                }

                String id = extractIdFromPath(pathInfo);
                CategoryResponse.ApiResponse<CategoryResponse.CategoryData> response =
                        categoryService.getCategoryDetail(id);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            }
        } catch (CategoryService.CategoryException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new CategoryResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new CategoryResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        // Kiểm tra quyền ADMIN hoặc STAFF
        String authError = authenticateRole(req, "ADMIN", "STAFF");
        if (authError != null) {
            int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
            JsonUtil.sendJsonResponse(resp, statusCode, new CategoryResponse.ApiResponse<>(statusCode, authError));
            return;
        }

        try {
            CategoryRequest.CreateCategoryRequest request = JsonUtil.fromJson(
                    req.getReader(), CategoryRequest.CreateCategoryRequest.class);
            CategoryResponse.ApiResponse<CategoryResponse.CategoryData> response =
                    categoryService.createCategory(request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
        } catch (CategoryService.CategoryException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new CategoryResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new CategoryResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        // Kiểm tra quyền ADMIN hoặc STAFF
        String authError = authenticateRole(req, "ADMIN", "STAFF");
        if (authError != null) {
            int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
            JsonUtil.sendJsonResponse(resp, statusCode, new CategoryResponse.ApiResponse<>(statusCode, authError));
            return;
        }

        String pathInfo = req.getPathInfo();
        String id = extractIdFromPath(pathInfo);
        if (id == null || id.isEmpty()) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new CategoryResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã loại sản phẩm trên đường dẫn"));
            return;
        }

        try {
            CategoryRequest.UpdateCategoryRequest request = JsonUtil.fromJson(
                    req.getReader(), CategoryRequest.UpdateCategoryRequest.class);
            CategoryResponse.ApiResponse<CategoryResponse.CategoryData> response =
                    categoryService.updateCategory(id, request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (CategoryService.CategoryException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new CategoryResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new CategoryResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        // Chỉ ADMIN mới có quyền xóa loại sản phẩm
        String authError = authenticateRole(req, "ADMIN");
        if (authError != null) {
            int statusCode = authError.contains("Chưa đăng nhập") ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
            JsonUtil.sendJsonResponse(resp, statusCode, new CategoryResponse.ApiResponse<>(statusCode, authError));
            return;
        }

        String pathInfo = req.getPathInfo();
        String id = extractIdFromPath(pathInfo);
        if (id == null || id.isEmpty()) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new CategoryResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã loại sản phẩm trên đường dẫn"));
            return;
        }

        try {
            CategoryResponse.ApiResponse<Void> response = categoryService.deleteCategory(id);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (CategoryService.CategoryException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new CategoryResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new CategoryResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
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
            return "Không có quyền thực hiện thao tác này";
        }

        for (String allowedRole : allowedRoles) {
            if (allowedRole.equalsIgnoreCase(role)) {
                return null; // Hợp lệ
            }
        }

        return "Không có quyền thực hiện thao tác này (yêu cầu vai trò: " + String.join(", ", allowedRoles) + ")";
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
