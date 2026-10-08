package com.mycompany.bachhoaxanhonline.module.auth;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
                case "/register":
                    handleRegister(req, resp);
                    break;
                case "/login":
                    handleLogin(req, resp);
                    break;
                case "/refresh":
                    handleRefresh(req, resp);
                    break;
                case "/logout":
                    handleLogout(req, resp);
                    break;
                default:
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            new ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại: /auth" + pathInfo));
                    break;
            }
        } catch (AuthService.AuthException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest.RegisterRequest request = JsonUtil.fromJson(req.getReader(), AuthRequest.RegisterRequest.class);
        ApiResponse<AuthResponse.RegisterData> response = authService.registerCustomer(request);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest.LoginRequest request = JsonUtil.fromJson(req.getReader(), AuthRequest.LoginRequest.class);
        AuthService.LoginResult result = authService.login(request);

        setRefreshTokenCookie(resp, result.getRefreshToken());
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, result.getResponse());
    }

    private void handleRefresh(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String refreshToken = extractRefreshToken(req);
        ApiResponse<AuthResponse.TokenData> response = authService.refreshToken(refreshToken);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String refreshToken = extractRefreshToken(req);
        authService.logout(refreshToken);
        clearRefreshTokenCookie(resp);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK,
                new ApiResponse<>(HttpServletResponse.SC_OK, "Đăng xuất thành công"));
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
