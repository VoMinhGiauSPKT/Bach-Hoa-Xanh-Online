package com.mycompany.bachhoaxanhonline.module.order;

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

@WebServlet(name = "OrderController", urlPatterns = {"/order", "/order/*"})
public class OrderController extends HttpServlet {

    private final OrderService orderService = new OrderService();

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
            sendError(resp, 401, "Chưa đăng nhập hoặc token không hợp lệ");
            return;
        }

        String role = JwtUtil.getRoleFromToken(token);
        String userId = JwtUtil.getUserIdFromToken(token);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                int page = 1;
                int limit = 10;
                try {
                    if (req.getParameter("page") != null) page = Integer.parseInt(req.getParameter("page"));
                    if (req.getParameter("limit") != null) limit = Integer.parseInt(req.getParameter("limit"));
                } catch (NumberFormatException ignored) {}

                ApiResponse<OrderResponse.OrderListData> response = orderService.getOrders(userId, role, page, limit);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } else {
                String orderId = extractIdFromPath(pathInfo);
                if (orderId == null) {
                    sendError(resp, 400, "Đường dẫn không hợp lệ");
                    return;
                }
                
                ApiResponse<OrderResponse.OrderDetailData> response = orderService.getOrderDetail(orderId, userId, role);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            }
        } catch (OrderService.OrderException e) {
            sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 500, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String token = extractToken(req);
        if (token == null || !JwtUtil.validateToken(token)) {
            sendError(resp, 401, "Chưa đăng nhập hoặc token không hợp lệ");
            return;
        }

        String role = JwtUtil.getRoleFromToken(token);
        String userId = JwtUtil.getUserIdFromToken(token);

        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            sendError(resp, 403, "Chỉ khách hàng mới có quyền đặt hàng");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                OrderRequest.CreateOrderRequest request = JsonUtil.fromJson(req.getReader(), OrderRequest.CreateOrderRequest.class);
                ApiResponse<OrderResponse.OrderDetailData> response = orderService.createOrder(userId, request);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
            } catch (OrderService.OrderException e) {
                sendError(resp, e.getStatusCode(), e.getMessage());
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                sendError(resp, 400, "Dữ liệu yêu cầu sai định dạng");
            } catch (Exception e) {
                e.printStackTrace();
                sendError(resp, 500, "Lỗi máy chủ nội bộ: " + e.getMessage());
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String token = extractToken(req);
        if (token == null || !JwtUtil.validateToken(token)) {
            sendError(resp, 401, "Chưa đăng nhập hoặc token không hợp lệ");
            return;
        }

        String role = JwtUtil.getRoleFromToken(token);
        if (!"STAFF".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            sendError(resp, 403, "Chỉ nhân viên hoặc quản trị viên mới có quyền xác nhận thanh toán");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.endsWith("/confirm-cod")) {
            String orderId = pathInfo.replace("/confirm-cod", "").replace("/", "");
            try {
                ApiResponse<OrderResponse.OrderDetailData> response = orderService.confirmCod(orderId);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } catch (OrderService.OrderException e) {
                sendError(resp, e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendError(resp, 500, "Lỗi hệ thống: " + e.getMessage());
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private String extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) return null;
        String cleaned = pathInfo.trim();
        if (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("/")) cleaned = cleaned.substring(0, cleaned.length() - 1);
        return cleaned.isEmpty() ? null : cleaned;
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
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
