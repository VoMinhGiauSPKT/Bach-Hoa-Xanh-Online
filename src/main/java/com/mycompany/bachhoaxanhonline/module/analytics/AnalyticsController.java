package com.mycompany.bachhoaxanhonline.module.analytics;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AnalyticsController", urlPatterns = {"/analytics", "/analytics/*"})
public class AnalyticsController extends HttpServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String token = extractToken(req);
        if (token == null || !JwtUtil.validateToken(token)) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ");
            return;
        }

        String role = JwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(role) && !"STAFF".equalsIgnoreCase(role)) {
            sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập dữ liệu báo cáo thống kê");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo) || "/overview".equalsIgnoreCase(pathInfo)) {
            String period = req.getParameter("period");
            ApiResponse<AnalyticsResponse.OverviewData> response = analyticsService.getOverview(period);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            return;
        }

        if ("/chart".equalsIgnoreCase(pathInfo)) {
            String period = req.getParameter("period");
            Integer days = null;
            try {
                if (req.getParameter("days") != null) {
                    days = Integer.parseInt(req.getParameter("days"));
                }
            } catch (NumberFormatException ignored) {}

            ApiResponse<AnalyticsResponse.ChartData> response = analyticsService.getChartData(period, days);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            return;
        }

        if ("/inventory-alerts".equalsIgnoreCase(pathInfo)) {
            ApiResponse<AnalyticsResponse.InventoryAlertsData> response = analyticsService.getInventoryAlerts();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            return;
        }

        if ("/top-products".equalsIgnoreCase(pathInfo)) {
            int limit = 5;
            try {
                if (req.getParameter("limit") != null) {
                    limit = Integer.parseInt(req.getParameter("limit"));
                }
            } catch (NumberFormatException ignored) {}

            String period = req.getParameter("period");
            ApiResponse<List<AnalyticsResponse.TopProductItem>> response = analyticsService.getTopProducts(limit, period);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            return;
        }

        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy endpoint phân tích: " + pathInfo);
    }

    private String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonUtil.sendJsonResponse(resp, status, new ApiResponse<>(status, message));
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
    }
}
