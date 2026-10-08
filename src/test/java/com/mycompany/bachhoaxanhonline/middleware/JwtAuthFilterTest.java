package com.mycompany.bachhoaxanhonline.middleware;

import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtAuthFilterTest {

    private JwtAuthFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new JwtAuthFilter();
    }

    @Test
    public void testIsPublicEndpoint() {
        // Whitelist routes
        Assertions.assertTrue(filter.isPublicEndpoint("/"));
        Assertions.assertTrue(filter.isPublicEndpoint("/index.html"));
        Assertions.assertTrue(filter.isPublicEndpoint("/auth/login"));
        Assertions.assertTrue(filter.isPublicEndpoint("/auth/register"));
        Assertions.assertTrue(filter.isPublicEndpoint("/auth/refresh"));
        Assertions.assertTrue(filter.isPublicEndpoint("/auth/logout"));

        // Static resources
        Assertions.assertTrue(filter.isPublicEndpoint("/assets/js/main.js"));
        Assertions.assertTrue(filter.isPublicEndpoint("/style.css"));
        Assertions.assertTrue(filter.isPublicEndpoint("/images/logo.png"));

        // Protected routes
        Assertions.assertFalse(filter.isPublicEndpoint("/employee"));
        Assertions.assertFalse(filter.isPublicEndpoint("/employee/create"));
        Assertions.assertFalse(filter.isPublicEndpoint("/admin/dashboard"));
        Assertions.assertFalse(filter.isPublicEndpoint("/customer/profile"));
        Assertions.assertFalse(filter.isPublicEndpoint("/cart/items"));
    }

    @Test
    public void testIsAuthorized() {
        // 1. Quản lý nhân viên / admin: Chỉ ADMIN
        Assertions.assertTrue(filter.isAuthorized("/employee", "POST", "ADMIN"));
        Assertions.assertTrue(filter.isAuthorized("/employee/add", "POST", "ADMIN"));
        Assertions.assertTrue(filter.isAuthorized("/admin/settings", "GET", "ADMIN"));

        Assertions.assertFalse(filter.isAuthorized("/employee", "POST", "STAFF"));
        Assertions.assertFalse(filter.isAuthorized("/employee", "POST", "CUSTOMER"));
        Assertions.assertFalse(filter.isAuthorized("/employee", "POST", null));

        // 2. Nội bộ staff: ADMIN hoặc STAFF
        Assertions.assertTrue(filter.isAuthorized("/staff/orders", "GET", "ADMIN"));
        Assertions.assertTrue(filter.isAuthorized("/staff/orders", "GET", "STAFF"));
        Assertions.assertFalse(filter.isAuthorized("/staff/orders", "GET", "CUSTOMER"));

        // 3. Nghiệp vụ khách hàng / chung: Bất kỳ role hợp lệ đã đăng nhập
        Assertions.assertTrue(filter.isAuthorized("/customer/profile", "GET", "CUSTOMER"));
        Assertions.assertTrue(filter.isAuthorized("/customer/profile", "GET", "ADMIN"));
        Assertions.assertTrue(filter.isAuthorized("/customer/profile", "GET", "STAFF"));
        Assertions.assertFalse(filter.isAuthorized("/customer/profile", "GET", null));
        Assertions.assertFalse(filter.isAuthorized("/customer/profile", "GET", ""));
    }

    @Test
    public void testPreflightOptionsRequest() throws IOException, ServletException {
        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        AtomicInteger respStatus = new AtomicInteger(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = createMockRequest("OPTIONS", "/employee", headers, reqAttrs);
        HttpServletResponse resp = createMockResponse(respStatus, out);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        Assertions.assertEquals(200, respStatus.get());
        Assertions.assertFalse(chainCalled.get(), "OPTIONS không được gọi tiếp vào chain");
    }

    @Test
    public void testPublicEndpointPassesThrough() throws IOException, ServletException {
        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        AtomicInteger respStatus = new AtomicInteger(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = createMockRequest("POST", "/auth/login", headers, reqAttrs);
        HttpServletResponse resp = createMockResponse(respStatus, out);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        Assertions.assertTrue(chainCalled.get(), "Public endpoint phải được gọi tiếp vào chain");
    }

    @Test
    public void testProtectedEndpointWithoutTokenReturns401() throws IOException, ServletException {
        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        AtomicInteger respStatus = new AtomicInteger(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = createMockRequest("POST", "/employee", headers, reqAttrs);
        HttpServletResponse resp = createMockResponse(respStatus, out);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        Assertions.assertFalse(chainCalled.get(), "Không có token thì không được vào controller");
        Assertions.assertEquals(401, respStatus.get());
        String body = out.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(body.contains("401"));
        Assertions.assertTrue(body.contains("Chưa đăng nhập"));
    }

    @Test
    public void testProtectedEndpointWithInvalidTokenReturns401() throws IOException, ServletException {
        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer invalid.jwt.token");
        AtomicInteger respStatus = new AtomicInteger(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = createMockRequest("POST", "/employee", headers, reqAttrs);
        HttpServletResponse resp = createMockResponse(respStatus, out);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        Assertions.assertFalse(chainCalled.get());
        Assertions.assertEquals(401, respStatus.get());
    }

    @Test
    public void testProtectedEndpointWithForbiddenRoleReturns403() throws IOException, ServletException {
        // Tạo token vai trò CUSTOMER
        String customerToken = JwtUtil.generateAccessToken("CUST_001", "Khách Hàng Test", "CUSTOMER");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + customerToken);
        AtomicInteger respStatus = new AtomicInteger(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = createMockRequest("POST", "/employee", headers, reqAttrs);
        HttpServletResponse resp = createMockResponse(respStatus, out);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        Assertions.assertFalse(chainCalled.get());
        Assertions.assertEquals(403, respStatus.get());
        String body = out.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(body.contains("403"));
        Assertions.assertTrue(body.contains("không có quyền"));
    }

    @Test
    public void testProtectedEndpointWithAdminRoleSuccess() throws IOException, ServletException {
        // Tạo token vai trò ADMIN
        String adminToken = JwtUtil.generateAccessToken("ADM_001", "Quản Trị Viên", "ADMIN");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + adminToken);
        AtomicInteger respStatus = new AtomicInteger(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = createMockRequest("POST", "/employee", headers, reqAttrs);
        HttpServletResponse resp = createMockResponse(respStatus, out);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        Assertions.assertTrue(chainCalled.get(), "Token ADMIN hợp lệ phải vào được controller");
        Assertions.assertEquals("ADM_001", SecurityContext.getUserId(req));
        Assertions.assertEquals("ADMIN", SecurityContext.getUserRole(req));
        Assertions.assertEquals("Quản Trị Viên", SecurityContext.getUserName(req));
        Assertions.assertTrue(SecurityContext.isAuthenticated(req));
        Assertions.assertTrue(SecurityContext.hasRole(req, "ADMIN"));
        Assertions.assertFalse(SecurityContext.hasRole(req, "CUSTOMER"));
    }

    // Helper tạo Mock HttpServletRequest bằng Dynamic Proxy
    private HttpServletRequest createMockRequest(String method, String uri, Map<String, String> headers, Map<String, Object> attributes) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                        String name = m.getName();
                        if ("getMethod".equals(name)) return method;
                        if ("getRequestURI".equals(name)) return uri;
                        if ("getContextPath".equals(name)) return "";
                        if ("getHeader".equals(name)) return headers.get(args[0].toString());
                        if ("getAttribute".equals(name)) return attributes.get(args[0].toString());
                        if ("setAttribute".equals(name)) {
                            attributes.put(args[0].toString(), args[1]);
                            return null;
                        }
                        if ("toString".equals(name)) return "MockHttpServletRequest[" + uri + "]";
                        return null;
                    }
                }
        );
    }

    // Helper tạo Mock HttpServletResponse bằng Dynamic Proxy
    private HttpServletResponse createMockResponse(AtomicInteger statusHolder, ByteArrayOutputStream out) {
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                        String name = m.getName();
                        if ("setStatus".equals(name)) {
                            statusHolder.set((Integer) args[0]);
                            return null;
                        }
                        if ("getStatus".equals(name)) return statusHolder.get();
                        if ("getWriter".equals(name)) return writer;
                        if ("setHeader".equals(name)) return null;
                        if ("setCharacterEncoding".equals(name)) return null;
                        if ("setContentType".equals(name)) return null;
                        if ("toString".equals(name)) return "MockHttpServletResponse";
                        return null;
                    }
                }
        );
    }
}
