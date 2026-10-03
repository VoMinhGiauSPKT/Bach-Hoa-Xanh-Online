package com.mycompany.bachhoaxanhonline.auth;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthTest {

    @AfterAll
    public static void tearDown() {
        JpaUtil.close();
    }

    @Test
    public void testPasswordHashing() {
        String raw = "MatKhau123@";
        String hashed = PasswordUtil.hash(raw);
        Assertions.assertNotNull(hashed);
        Assertions.assertTrue(PasswordUtil.verify(raw, hashed));
        Assertions.assertFalse(PasswordUtil.verify("wrongpass", hashed));
    }

    @Test
    public void testJwtGenerationAndValidation() {
        String userId = "ND_123456";
        String userName = "Nguyen Van A";
        String role = "CUSTOMER";

        String accessToken = JwtUtil.generateAccessToken(userId, userName, role);
        Assertions.assertNotNull(accessToken);
        Assertions.assertTrue(JwtUtil.validateToken(accessToken));
        Assertions.assertEquals(userId, JwtUtil.getUserIdFromToken(accessToken));
        Assertions.assertEquals(role, JwtUtil.getRoleFromToken(accessToken));

        String refreshToken = JwtUtil.generateRefreshToken(userId);
        Assertions.assertNotNull(refreshToken);
        Assertions.assertTrue(JwtUtil.validateToken(refreshToken));
        Assertions.assertEquals(userId, JwtUtil.getUserIdFromToken(refreshToken));
    }

    @Test
    public void testNeonDatabaseConnection() {
        EntityManager em = JpaUtil.getEntityManager();
        Assertions.assertNotNull(em);
        try {
            AuthRepository repo = new AuthRepository();
            boolean exists = repo.existsByPhone("0000000000");
            Assertions.assertFalse(exists);
        } finally {
            em.close();
        }
    }

    @Test
    public void testSingleDeviceLoginAndTokenRevocation() {
        AuthService service = new AuthService();
        String testPhone = "0999" + (int)(Math.random() * 900000 + 100000);

        // 1. Đăng ký tài khoản
        AuthRequest.RegisterRequest regReq = new AuthRequest.RegisterRequest(
                "Test Single Device", testPhone, "Password123@", null);
        AuthResponse.RegisterResponse regResp = service.registerCustomer(regReq);
        Assertions.assertNotNull(regResp.getUserId());

        // 2. Máy 1 Đăng nhập -> Lưu token_1 vào DB
        AuthRequest.LoginRequest loginReq = new AuthRequest.LoginRequest(testPhone, "Password123@");
        AuthService.LoginResult loginResultDevice1 = service.loginCustomer(loginReq);
        String tokenDevice1 = loginResultDevice1.getRefreshToken();
        Assertions.assertNotNull(tokenDevice1);

        // 3. Máy 2 Đăng nhập -> Ghi đè token_2 vào DB (Đá máy 1 ra)
        AuthService.LoginResult loginResultDevice2 = service.loginCustomer(loginReq);
        String tokenDevice2 = loginResultDevice2.getRefreshToken();
        Assertions.assertNotNull(tokenDevice2);
        Assertions.assertNotEquals(tokenDevice1, tokenDevice2);

        // 4. Máy 1 cố gắng refresh bằng token_1 -> PHẢI BỊ TỪ CHỐI (401)
        AuthService.AuthException ex = Assertions.assertThrows(
                AuthService.AuthException.class,
                () -> service.refreshToken(tokenDevice1)
        );
        Assertions.assertEquals(401, ex.getStatusCode());
        Assertions.assertEquals("TOKEN_REVOKED", ex.getErrorCode());

        // 5. Máy 2 refresh bằng token_2 -> THÀNH CÔNG!
        AuthResponse.TokenResponse refreshResp = service.refreshToken(tokenDevice2);
        Assertions.assertNotNull(refreshResp.getAccessToken());

        // 6. Máy 2 đăng xuất -> xóa token trong DB
        service.logout(tokenDevice2);

        // 7. Máy 2 cố refresh lại sau khi đã logout -> BỊ TỪ CHỐI (401)
        Assertions.assertThrows(
                AuthService.AuthException.class,
                () -> service.refreshToken(tokenDevice2)
        );
    }
}
