package com.mycompany.bachhoaxanhonline.auth;

import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "AuthController", urlPatterns = {"/auth/*"})
public class AuthController extends HttpServlet {

    private final AuthService authService = new AuthService();

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
            switch (pathInfo) {
                case "/customer/register":
                    handleCustomerRegister(req, resp);
                    break;
                case "/customer/login":
                    handleCustomerLogin(req, resp);
                    break;
                case "/admin/login":
                    handleAdminLogin(req, resp);
                    break;
                case "/logout":
                    handleLogout(req, resp);
                    break;
                case "/refresh-token":
                    handleRefreshToken(req, resp);
                    break;
                default:
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            new AuthResponse.ErrorResponse("NOT_FOUND", "Endpoint không tồn tại: /auth" + pathInfo));
                    break;
            }
        } catch (AuthService.AuthException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new AuthResponse.ErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new AuthResponse.ErrorResponse("INTERNAL_SERVER_ERROR", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private void handleCustomerRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest.RegisterRequest request = JsonUtil.fromJson(req.getReader(), AuthRequest.RegisterRequest.class);
        AuthResponse.RegisterResponse response = authService.registerCustomer(request);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
    }

    private void handleCustomerLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest.LoginRequest request = JsonUtil.fromJson(req.getReader(), AuthRequest.LoginRequest.class);
        AuthService.LoginResult result = authService.loginCustomer(request);

        setRefreshTokenCookie(resp, result.getRefreshToken());
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, result.getResponse());
    }

    private void handleAdminLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest.LoginRequest request = JsonUtil.fromJson(req.getReader(), AuthRequest.LoginRequest.class);
        AuthService.LoginResult result = authService.loginAdmin(request);

        setRefreshTokenCookie(resp, result.getRefreshToken());
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, result.getResponse());
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = extractRefreshToken(req);
        String authHeader = req.getHeader("Authorization");

        // Kiểm tra xem có token hợp lệ không
        boolean hasValidToken = (token != null && JwtUtil.validateToken(token))
                || (authHeader != null && authHeader.startsWith("Bearer ") && JwtUtil.validateToken(authHeader.substring(7)));

        if (!hasValidToken) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new AuthResponse.ErrorResponse("INVALID_TOKEN", "Token không hợp lệ"));
            return;
        }

        // Vô hiệu hóa token trong database
        String tokenToRevoke = (token != null && JwtUtil.validateToken(token))
                ? token
                : authHeader.substring(7);
        authService.logout(tokenToRevoke);

        // Xóa Refresh Token trong cookie
        clearRefreshTokenCookie(resp);

        // Hủy session nếu có
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK,
                new AuthResponse.MessageResponse("Đăng xuất thành công"));
    }

    private void handleRefreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String refreshToken = extractRefreshToken(req);

        // Hỗ trợ đọc thêm từ Request Body nếu client không dùng cookie
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            try {
                java.util.Map<?, ?> body = JsonUtil.fromJson(req.getReader(), java.util.Map.class);
                if (body != null && body.containsKey("refreshToken")) {
                    refreshToken = (String) body.get("refreshToken");
                }
            } catch (Exception ignored) {
            }
        }

        AuthResponse.TokenResponse response = authService.refreshToken(refreshToken);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void setRefreshTokenCookie(HttpServletResponse resp, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        resp.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse resp) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }

    private String extractRefreshToken(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
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
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
