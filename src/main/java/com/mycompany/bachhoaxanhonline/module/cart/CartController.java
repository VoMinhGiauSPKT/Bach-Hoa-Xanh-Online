package com.mycompany.bachhoaxanhonline.module.cart;

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

@WebServlet(name = "CartController", urlPatterns = {"/cart", "/cart/*"})
public class CartController extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        
        String customerId = authenticateCustomer(req);
        if (customerId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ"));
            return;
        }

        try {
            ApiResponse<CartResponse.CartDetailData> response = cartService.getCart(customerId);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (CartService.CartException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(), new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req);
        if (customerId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ"));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/items")) {
            try {
                CartRequest.AddItemRequest request = JsonUtil.fromJson(req.getReader(), CartRequest.AddItemRequest.class);
                ApiResponse<CartResponse.AddItemData> response = cartService.addItemToCart(customerId, request);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } catch (CartService.CartException e) {
                JsonUtil.sendJsonResponse(resp, e.getStatusCode(), new ApiResponse<>(e.getStatusCode(), e.getMessage()));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu yêu cầu sai định dạng"));
            } catch (Exception e) {
                e.printStackTrace();
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req);
        if (customerId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ"));
            return;
        }

        String pathInfo = req.getPathInfo(); // Expected: /items/{lineItemId}
        if (pathInfo != null && pathInfo.startsWith("/items/")) {
            try {
                String idStr = pathInfo.substring(7);
                Long lineItemId = Long.parseLong(idStr);
                
                CartRequest.UpdateItemRequest request = JsonUtil.fromJson(req.getReader(), CartRequest.UpdateItemRequest.class);
                ApiResponse<CartResponse.UpdateItemData> response = cartService.updateItemQuantity(customerId, lineItemId, request);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } catch (NumberFormatException e) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ"));
            } catch (CartService.CartException e) {
                JsonUtil.sendJsonResponse(resp, e.getStatusCode(), new ApiResponse<>(e.getStatusCode(), e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req);
        if (customerId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ"));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            if (pathInfo.equals("/clear")) {
                try {
                    ApiResponse<CartResponse.CartDetailData> response = cartService.clearCart(customerId);
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                } catch (CartService.CartException e) {
                    JsonUtil.sendJsonResponse(resp, e.getStatusCode(), new ApiResponse<>(e.getStatusCode(), e.getMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ: " + e.getMessage()));
                }
            } else if (pathInfo.startsWith("/items/")) {
                try {
                    String idStr = pathInfo.substring(7);
                    Long lineItemId = Long.parseLong(idStr);
                    
                    ApiResponse<CartResponse.CartDetailData> response = cartService.removeItem(customerId, lineItemId);
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                } catch (NumberFormatException e) {
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                            new ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ"));
                } catch (CartService.CartException e) {
                    JsonUtil.sendJsonResponse(resp, e.getStatusCode(), new ApiResponse<>(e.getStatusCode(), e.getMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ: " + e.getMessage()));
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String authenticateCustomer(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        if (!JwtUtil.validateToken(token)) {
            return null;
        }
        String role = JwtUtil.getRoleFromToken(token);
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return null;
        }
        return JwtUtil.getUserIdFromToken(token);
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
