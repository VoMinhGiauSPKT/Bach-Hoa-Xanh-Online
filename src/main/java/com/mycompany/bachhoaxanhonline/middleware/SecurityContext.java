package com.mycompany.bachhoaxanhonline.middleware;

import jakarta.servlet.http.HttpServletRequest;

/**
 * SecurityContext chứa thông tin ngữ cảnh bảo mật của người dùng hiện tại
 * được JwtAuthFilter giải mã từ Access Token và gắn vào HttpServletRequest.
 */
public class SecurityContext {

    public static final String ATTR_USER_ID = "CURRENT_USER_ID";
    public static final String ATTR_USER_ROLE = "CURRENT_USER_ROLE";
    public static final String ATTR_USER_NAME = "CURRENT_USER_NAME";
    public static final String ATTR_ACCESS_TOKEN = "CURRENT_ACCESS_TOKEN";

    /**
     * Gắn thông tin người dùng vào request attributes
     */
    public static void setContext(HttpServletRequest request, String userId, String role, String userName, String token) {
        if (request != null) {
            request.setAttribute(ATTR_USER_ID, userId);
            request.setAttribute(ATTR_USER_ROLE, role);
            request.setAttribute(ATTR_USER_NAME, userName);
            request.setAttribute(ATTR_ACCESS_TOKEN, token);
        }
    }

    /**
     * Lấy ID người dùng hiện tại từ request
     */
    public static String getUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object val = request.getAttribute(ATTR_USER_ID);
        return val != null ? val.toString() : null;
    }

    /**
     * Lấy Role của người dùng hiện tại (ADMIN, STAFF, CUSTOMER)
     */
    public static String getUserRole(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object val = request.getAttribute(ATTR_USER_ROLE);
        return val != null ? val.toString() : null;
    }

    /**
     * Lấy Tên hiển thị của người dùng hiện tại
     */
    public static String getUserName(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object val = request.getAttribute(ATTR_USER_NAME);
        return val != null ? val.toString() : null;
    }

    /**
     * Lấy Access Token từ request
     */
    public static String getAccessToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object val = request.getAttribute(ATTR_ACCESS_TOKEN);
        return val != null ? val.toString() : null;
    }

    /**
     * Kiểm tra người dùng đã được xác thực chưa
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return getUserId(request) != null;
    }

    /**
     * Kiểm tra người dùng có vai trò cụ thể hay không
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        if (role == null) {
            return false;
        }
        String currentRole = getUserRole(request);
        return currentRole != null && currentRole.equalsIgnoreCase(role);
    }
}
