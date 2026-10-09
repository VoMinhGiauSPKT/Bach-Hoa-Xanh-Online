package com.mycompany.bachhoaxanhonline.module.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.payos.type.Webhook;

import java.io.IOException;

@WebServlet(name = "PaymentController", urlPatterns = {"/payment", "/payment/*"})
public class PaymentController extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        try {
            if (pathInfo.startsWith("/create/")) {
                // Endpoint: POST /payment/create/{orderId}
                String token = extractToken(req);
                if (token == null || !JwtUtil.validateToken(token)) {
                    sendError(resp, 401, "Chưa đăng nhập hoặc token không hợp lệ");
                    return;
                }

                String role = JwtUtil.getRoleFromToken(token);
                String userId = JwtUtil.getUserIdFromToken(token);
                String orderId = pathInfo.substring("/create/".length()).replaceAll("/$", "").trim();

                if (orderId.isEmpty()) {
                    sendError(resp, 400, "Đường dẫn không hợp lệ, thiếu mã đơn hàng");
                    return;
                }

                ApiResponse<PaymentResponse.PaymentLinkData> response = paymentService.createPaymentLink(orderId, userId, role);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);

            } else if (pathInfo.equals("/webhook") || pathInfo.equals("/webhook/")) {
                // Endpoint: POST /payment/webhook (PayOS gọi vào để thông báo trạng thái thanh toán)
                try {
                    JsonNode jsonNode = objectMapper.readTree(req.getReader());
                    Webhook webhookBody = objectMapper.convertValue(jsonNode, Webhook.class);

                    ApiResponse<String> response = paymentService.processWebhook(webhookBody);
                    JsonUtil.sendJsonResponse(resp, response.getStatus(), response);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError(resp, 400, "Dữ liệu webhook không hợp lệ: " + e.getMessage());
                }

            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (PaymentService.PaymentException e) {
            sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 500, "Lỗi máy chủ nội bộ: " + e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonUtil.sendJsonResponse(resp, status, new ApiResponse<>(status, message, null));
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
