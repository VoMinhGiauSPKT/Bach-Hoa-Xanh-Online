package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
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

        // Xác thực token và kiểm tra quyền ADMIN đã được thực hiện tập trung bởi JwtAuthFilter

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
