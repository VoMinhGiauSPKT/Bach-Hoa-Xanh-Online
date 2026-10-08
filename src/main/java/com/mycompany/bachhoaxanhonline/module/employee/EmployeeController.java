package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.middleware.SecurityContext;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "EmployeeController", urlPatterns = { "/employee", "/employee/*" })
public class EmployeeController extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Hỗ trợ phương thức HTTP PATCH trong HttpServlet
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        try {
            String id = extractIdFromPath(req.getPathInfo());
            if (id != null) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không hợp lệ"));
                return;
            }

            handleCreateEmployee(req, resp);
        } catch (EmployeeService.EmployeeException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new EmployeeResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        try {
            String id = extractIdFromPath(req.getPathInfo());
            if (id == null) {
                handleGetEmployees(req, resp);
            } else {
                handleGetEmployeeById(req, resp, id);
            }
        } catch (EmployeeService.EmployeeException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new EmployeeResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        try {
            String id = extractIdFromPath(req.getPathInfo());
            if (id == null) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST,
                                "Thiếu ID nhân viên trong đường dẫn"));
                return;
            }

            handleUpdateEmployee(req, resp, id);
        } catch (EmployeeService.EmployeeException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new EmployeeResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        try {
            String id = extractIdFromPath(req.getPathInfo());
            if (id == null) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST,
                                "Thiếu ID nhân viên trong đường dẫn"));
                return;
            }

            handleUpdateStatus(req, resp, id);
        } catch (EmployeeService.EmployeeException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new EmployeeResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new EmployeeResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private void handleCreateEmployee(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        EmployeeRequest.CreateEmployeeRequest request = JsonUtil.fromJson(
                req.getReader(), EmployeeRequest.CreateEmployeeRequest.class);
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> response = employeeService.createEmployee(request);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
    }

    private void handleGetEmployees(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int page = getIntParameter(req, "page", 1);
        int limit = getIntParameter(req, "limit", 10);
        String keyword = req.getParameter("keyword");
        String position = req.getParameter("position");
        Boolean status = getBooleanParameter(req, "status");

        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeListData> response = employeeService
                .getEmployees(page, limit, keyword, position, status);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleGetEmployeeById(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeDetailData> response = employeeService
                .getEmployeeById(id);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleUpdateEmployee(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        EmployeeRequest.UpdateEmployeeRequest request = JsonUtil.fromJson(
                req.getReader(), EmployeeRequest.UpdateEmployeeRequest.class);
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> response = employeeService.updateEmployee(id,
                request);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        EmployeeRequest.UpdateStatusRequest request = JsonUtil.fromJson(
                req.getReader(), EmployeeRequest.UpdateStatusRequest.class);
        String currentAdminId = SecurityContext.getUserId(req);
        EmployeeResponse.ApiResponse<EmployeeResponse.UpdateStatusData> response = employeeService.updateStatus(id,
                request, currentAdminId);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private String extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
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

    private int getIntParameter(HttpServletRequest req, String name, int defaultValue) {
        String val = req.getParameter(name);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Boolean getBooleanParameter(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        return Boolean.valueOf(val.trim());
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
