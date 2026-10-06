package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "EmployeeController", urlPatterns = {"/employee", "/employee/*"})
public class EmployeeController extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        // 1. Middleware / AuthFilter: Kiểm tra và xác thực Bearer Access Token
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        String token = authHeader.substring(7).trim();
        if (!JwtUtil.validateToken(token)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        // 2. Middleware / AdminRoleFilter: Kiểm tra quyền ADMIN
        String role = JwtUtil.getRoleFromToken(token);
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_FORBIDDEN, "Người gọi không phải là ADMIN"));
            return;
        }

        // 3. Xử lý tạo tài khoản nhân viên
        try {
            EmployeeRequest.CreateEmployeeRequest request = JsonUtil.fromJson(
                    req.getReader(), EmployeeRequest.CreateEmployeeRequest.class);
            EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> response = employeeService.createEmployee(request);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
        } catch (EmployeeService.EmployeeException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new EmployeeResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
