package com.mycompany.bachhoaxanhonline.module.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mycompany.bachhoaxanhonline.middleware.SecurityContext;
import com.mycompany.bachhoaxanhonline.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserController", urlPatterns = { "/user", "/user/*" })
public class UserController extends HttpServlet {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        try {
            String action = resolveAction(req);
            if ("/profile".equalsIgnoreCase(action)) {
                handleGetProfile(req, resp);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new UserResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại"));
            }
        } catch (UserService.UserException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new UserResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new UserResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCorsHeaders(req, resp);

        try {
            String action = resolveAction(req);
            if ("/change-password".equalsIgnoreCase(action)) {
                handleChangePassword(req, resp);
            } else {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        new UserResponse.ApiResponse<>(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại"));
            }
        } catch (UserService.UserException e) {
            JsonUtil.sendJsonResponse(resp, e.getStatusCode(),
                    new UserResponse.ApiResponse<>(e.getStatusCode(), e.getMessage()));
        } catch (JsonProcessingException e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new UserResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST,
                            "Dữ liệu gửi lên không đúng định dạng JSON: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new UserResponse.ApiResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    private void handleGetProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = resolveUserId(req);
        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new UserResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED,
                            "Chưa đăng nhập, thiếu token hoặc token đã hết hạn / không hợp lệ"));
            return;
        }

        UserResponse.ApiResponse<UserResponse.UserProfileData> response = userService.getProfile(userId);
        JsonUtil.sendJsonResponse(resp, response.getStatus(), response);
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = resolveUserId(req);
        if (userId == null) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    new UserResponse.ApiResponse<>(HttpServletResponse.SC_UNAUTHORIZED,
                            "Chưa đăng nhập hoặc token không hợp lệ / hết hạn"));
            return;
        }

        UserRequest.ChangePasswordRequest request;
        try {
            request = JsonUtil.fromJson(req.getReader(), UserRequest.ChangePasswordRequest.class);
        } catch (Exception e) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new UserResponse.ApiResponse<>(HttpServletResponse.SC_BAD_REQUEST,
                            "Dữ liệu gửi lên không đúng định dạng JSON: " + e.getMessage()));
            return;
        }

        UserResponse.ApiResponse<Void> response = userService.changePassword(userId, request);

        clearRefreshTokenCookie(resp);

        JsonUtil.sendJsonResponse(resp, response.getStatus(), response);
    }

    private String resolveUserId(HttpServletRequest req) {
        return SecurityContext.getUserId(req);
    }

    private String resolveAction(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.trim().isEmpty()) {
            return pathInfo.trim();
        }
        String uri = req.getRequestURI();
        if (uri != null) {
            if (uri.endsWith("/profile")) {
                return "/profile";
            }
            if (uri.endsWith("/change-password")) {
                return "/change-password";
            }
        }
        return "";
    }

    private void clearRefreshTokenCookie(HttpServletResponse resp) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        resp.addCookie(cookie);
    }

    private void setupCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
    }
}
