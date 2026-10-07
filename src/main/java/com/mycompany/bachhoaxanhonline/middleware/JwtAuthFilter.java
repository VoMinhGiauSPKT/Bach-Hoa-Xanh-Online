package com.mycompany.bachhoaxanhonline.middleware;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Middleware bộ lọc xác thực tập trung:
 * 1. Đón nhận mọi request gửi tới ứng dụng
 * 2. Cấu hình CORS headers & giải phóng CORS preflight (OPTIONS)
 * 3. Cho phép các public endpoints đi qua không cần token
 * 4. Kiểm tra và xác thực Bearer Access Token đối với các protected endpoints
 * 5. Phân quyền (RBAC) theo URL và vai trò người dùng (ADMIN, STAFF, CUSTOMER)
 * 6. Gắn thông tin người dùng vào SecurityContext để các controller tái sử dụng
 */
@WebFilter(filterName = "JwtAuthFilter", urlPatterns = {"/*"})
public class JwtAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Khởi tạo filter nếu cần thiết
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Cấu hình CORS Headers cho mọi request
        setupCorsHeaders(httpRequest, httpResponse);

        // 2. Nếu là HTTP OPTIONS (CORS preflight request), chấp nhận ngay với HTTP 200 OK
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 3. Lấy đường dẫn tương đối (loại bỏ context path)
        String path = getRelativePath(httpRequest);

        // 4. Cho phép các route công khai (Public Endpoints) đi qua
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 5. Kiểm tra Bearer Access Token trong Authorization Header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chưa đăng nhập hoặc token không hợp lệ / hết hạn");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !JwtUtil.validateToken(token)) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chưa đăng nhập hoặc token không hợp lệ / hết hạn");
            return;
        }

        // 6. Trích xuất thông tin người dùng từ JWT và lưu vào SecurityContext
        String userId = JwtUtil.getUserIdFromToken(token);
        String role = JwtUtil.getRoleFromToken(token);
        String userName = null;
        try {
            Claims claims = JwtUtil.parseToken(token);
            userName = claims.get("ten", String.class);
        } catch (Exception ignored) {
        }

        SecurityContext.setContext(httpRequest, userId, role, userName, token);

        // 7. Phân quyền truy cập dựa trên vai trò (Role-Based Access Control - RBAC)
        if (!isAuthorized(path, httpRequest.getMethod(), role)) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập endpoint này");
            return;
        }

        // 8. Chuyển tiếp request cho filter tiếp theo hoặc Servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Dọn dẹp tài nguyên nếu có
    }

    /**
     * Kiểm tra endpoint có phải là route công khai không cần đăng nhập hay không.
     */
    public boolean isPublicEndpoint(String path) {
        if (path == null || path.isEmpty() || "/".equals(path) || "/index.html".equals(path)) {
            return true;
        }

        // Auth endpoints công khai
        if (path.equals("/auth/login") || path.startsWith("/auth/login/")
                || path.equals("/auth/register") || path.startsWith("/auth/register/")
                || path.equals("/auth/refresh") || path.startsWith("/auth/refresh/")
                || path.equals("/auth/logout") || path.startsWith("/auth/logout/")) {
            return true;
        }

        // Đánh giá sản phẩm công khai (GET /review/product/:productId)
        if (path.equals("/review/product") || path.startsWith("/review/product/")) {
            return true;
        }

        // Tài nguyên tĩnh (CSS, JS, Fonts, Images)
        if (path.startsWith("/assets/") || path.startsWith("/static/")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".html") || path.endsWith(".ico")
                || path.endsWith(".png") || path.endsWith(".jpg")
                || path.endsWith(".jpeg") || path.endsWith(".gif")
                || path.endsWith(".svg") || path.endsWith(".woff")
                || path.endsWith(".woff2") || path.endsWith(".ttf")) {
            return true;
        }

        return false;
    }

    /**
     * Kiểm tra vai trò của người dùng có quyền gọi endpoint hay không.
     */
    public boolean isAuthorized(String path, String method, String role) {
        if (path == null) {
            return false;
        }

        // Endpoint quản trị hoặc nhân sự (/employee, /admin): Chỉ ADMIN
        if (path.equals("/employee") || path.startsWith("/employee/")
                || path.equals("/admin") || path.startsWith("/admin/")) {
            return "ADMIN".equalsIgnoreCase(role);
        }

        // Endpoint nhân viên nội bộ (/staff) hoặc kiểm duyệt đánh giá (GET /review): Cho phép ADMIN và STAFF
        if (path.equals("/staff") || path.startsWith("/staff/")
                || (path.equals("/review") && "GET".equalsIgnoreCase(method))) {
            return "ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
        }

        // Các endpoint nghiệp vụ khác: Yêu cầu người dùng đã được cấp quyền hợp lệ
        return role != null && !role.trim().isEmpty();
    }

    /**
     * Chuẩn hóa đường dẫn URI (loại bỏ context path).
     */
    public String getRelativePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri == null || uri.isEmpty()) {
            return "/";
        }
        return uri;
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

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> errorMap = new LinkedHashMap<>();
        errorMap.put("status", statusCode);
        errorMap.put("message", message);
        JsonUtil.sendJsonResponse(response, statusCode, errorMap);
    }
}
