package com.mycompany.bachhoaxanhonline.module.address;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AddressController", urlPatterns = {"/address", "/address/*"})
public class AddressController extends HttpServlet {

    private final AddressService addressService = new AddressService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req, resp);
        if (customerId == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /address" + pathInfo));
            return;
        }

        try {
            AddressResponse.ApiResponse<?> response = addressService.getAddresses(customerId);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (AddressService.AddressException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new AddressResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req, resp);
        if (customerId == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /address" + pathInfo));
            return;
        }

        try {
            AddressRequest.CreateAddressRequest request = JsonUtil.fromJson(
                    req.getReader(), AddressRequest.CreateAddressRequest.class);
            AddressResponse.ApiResponse<?> response = addressService.createAddress(customerId, request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
        } catch (AddressService.AddressException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new AddressResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu gửi lên không đúng định dạng JSON"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req, resp);
        if (customerId == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        Long addressId = parseSingleId(pathInfo);
        if (addressId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Mã địa chỉ không hợp lệ trên đường dẫn"));
            return;
        }

        try {
            AddressRequest.UpdateAddressRequest request = JsonUtil.fromJson(
                    req.getReader(), AddressRequest.UpdateAddressRequest.class);
            AddressResponse.ApiResponse<?> response = addressService.updateAddress(customerId, addressId, request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (AddressService.AddressException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new AddressResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu gửi lên không đúng định dạng JSON"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req, resp);
        if (customerId == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        Long addressId = parseDefaultActionId(pathInfo);
        if (addressId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /address" + (pathInfo != null ? pathInfo : "")));
            return;
        }

        try {
            AddressResponse.ApiResponse<?> response = addressService.setDefaultAddress(customerId, addressId);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (AddressService.AddressException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new AddressResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        String customerId = authenticateCustomer(req, resp);
        if (customerId == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        Long addressId = parseSingleId(pathInfo);
        if (addressId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST, "Mã địa chỉ không hợp lệ trên đường dẫn"));
            return;
        }

        try {
            AddressResponse.ApiResponse<?> response = addressService.deleteAddress(customerId, addressId);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (AddressService.AddressException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new AddressResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private String authenticateCustomer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return null;
        }

        String token = authHeader.substring(7).trim();
        if (!JwtUtil.validateToken(token)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return null;
        }

        String userId;
        String role;
        try {
            userId = JwtUtil.getUserIdFromToken(token);
            role = JwtUtil.getRoleFromToken(token);
        } catch (Exception e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return null;
        }

        if (userId == null || role == null || !"CUSTOMER".equalsIgnoreCase(role)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_FORBIDDEN, "Tài khoản bị khóa hoặc không phải role khách hàng"));
            return null;
        }

        if (addressService.isAccountLockedOrNotCustomer(userId)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                    new AddressResponse.ApiResponse<>(HttpServletResponse.SC_FORBIDDEN, "Tài khoản bị khóa hoặc không phải role khách hàng"));
            return null;
        }

        return userId;
    }

    private Long parseSingleId(String pathInfo) {
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
        if (cleaned.contains("/")) {
            return null;
        }
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseDefaultActionId(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            return null;
        }
        String cleaned = pathInfo.trim();
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        // Kỳ vọng dạng: {id}/default
        String[] parts = cleaned.split("/");
        if (parts.length == 2 && "default".equalsIgnoreCase(parts[1])) {
            try {
                return Long.parseLong(parts[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
