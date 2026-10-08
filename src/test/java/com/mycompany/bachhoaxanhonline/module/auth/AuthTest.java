package com.mycompany.bachhoaxanhonline.module.auth;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthTest {

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
    public void testRegisterCustomer_SuccessAndConflict() {
        AuthService service = new AuthService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "cust_" + rand;
        String email = "cust_" + rand + "@example.com";
        String phone = "09" + String.format("%08d", rand);

        // 1. Đăng ký thành công (201)
        AuthRequest.RegisterRequest req = new AuthRequest.RegisterRequest(
                username, "Nguyen Van Test", email, phone, "Password123!", LocalDate.of(2000, 1, 1));
        ApiResponse<AuthResponse.RegisterData> resp = service.registerCustomer(req);

        Assertions.assertEquals(201, resp.getStatus());
        Assertions.assertEquals("Đăng ký thành công", resp.getMessage());
        Assertions.assertNotNull(resp.getData());
        Assertions.assertEquals(username, resp.getData().getUsername());
        Assertions.assertEquals(email, resp.getData().getEmail());
        Assertions.assertEquals(phone, resp.getData().getPhoneNumber());
        Assertions.assertNotNull(resp.getData().getCustomerId());

        // 2. Trùng username -> 409
        AuthRequest.RegisterRequest dupUserReq = new AuthRequest.RegisterRequest(
                username, "Other Name", "other_" + rand + "@example.com", "08" + String.format("%08d", rand), "Password123!", null);
        AuthService.AuthException exUser = Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.registerCustomer(dupUserReq));
        Assertions.assertEquals(409, exUser.getStatusCode());

        // 3. Trùng email -> 409
        AuthRequest.RegisterRequest dupEmailReq = new AuthRequest.RegisterRequest(
                "other_u_" + rand, "Other Name", email, "08" + String.format("%08d", rand), "Password123!", null);
        AuthService.AuthException exEmail = Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.registerCustomer(dupEmailReq));
        Assertions.assertEquals(409, exEmail.getStatusCode());

        // 4. Trùng phone -> 409
        AuthRequest.RegisterRequest dupPhoneReq = new AuthRequest.RegisterRequest(
                "other_u2_" + rand, "Other Name", "other2_" + rand + "@example.com", phone, "Password123!", null);
        AuthService.AuthException exPhone = Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.registerCustomer(dupPhoneReq));
        Assertions.assertEquals(409, exPhone.getStatusCode());
    }

    @Test
    public void testLoginWithUsernameOnly() {
        AuthService service = new AuthService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "logintest_" + rand;
        String email = "logintest_" + rand + "@example.com";
        String phone = "09" + String.format("%08d", rand);
        String pass = "Password123!";

        // Đăng ký trước
        service.registerCustomer(new AuthRequest.RegisterRequest(
                username, "User Dang Nhap", email, phone, pass, LocalDate.of(1995, 5, 20)));

        // 1. Đăng nhập đúng bằng username -> 200 OK
        AuthRequest.LoginRequest validReq = new AuthRequest.LoginRequest(username, pass);
        AuthService.LoginResult loginResult = service.login(validReq);
        Assertions.assertEquals(200, loginResult.getResponse().getStatus());
        Assertions.assertNotNull(loginResult.getResponse().getData());
        Assertions.assertEquals("CUSTOMER", loginResult.getResponse().getData().getUserType());
        Assertions.assertNotNull(loginResult.getResponse().getData().getCustomer());
        Assertions.assertEquals(username, loginResult.getResponse().getData().getCustomer().getUsername());
        Assertions.assertNotNull(loginResult.getResponse().getData().getAccessToken());
        Assertions.assertNotNull(loginResult.getRefreshToken());

        // 2. Cố gắng đăng nhập bằng số điện thoại -> Phải bị từ chối 401 (chỉ cho phép username)
        AuthRequest.LoginRequest phoneReq = new AuthRequest.LoginRequest(phone, pass);
        AuthService.AuthException exPhone = Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.login(phoneReq));
        Assertions.assertEquals(401, exPhone.getStatusCode());

        // 3. Đăng nhập sai mật khẩu -> 401
        AuthRequest.LoginRequest wrongPassReq = new AuthRequest.LoginRequest(username, "WrongPass123!");
        AuthService.AuthException exPass = Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.login(wrongPassReq));
        Assertions.assertEquals(401, exPass.getStatusCode());
    }

    @Test
    public void testSingleDeviceLoginAndRefreshRevocation() {
        AuthService service = new AuthService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "device_" + rand;
        String email = "device_" + rand + "@example.com";
        String phone = "09" + String.format("%08d", rand);
        String pass = "Password123!";

        service.registerCustomer(new AuthRequest.RegisterRequest(
                username, "User Single Device", email, phone, pass, null));

        // 1. Thiết bị 1 đăng nhập -> Lấy token 1
        AuthService.LoginResult loginDev1 = service.login(new AuthRequest.LoginRequest(username, pass));
        String tokenDev1 = loginDev1.getRefreshToken();
        Assertions.assertNotNull(tokenDev1);

        // 2. Thiết bị 2 đăng nhập -> Ghi đè token 2 vào DB
        AuthService.LoginResult loginDev2 = service.login(new AuthRequest.LoginRequest(username, pass));
        String tokenDev2 = loginDev2.getRefreshToken();
        Assertions.assertNotNull(tokenDev2);
        Assertions.assertNotEquals(tokenDev1, tokenDev2);

        // 3. Thiết bị 1 cố refresh bằng token 1 cũ -> Phải bị từ chối 403
        AuthService.AuthException exRevoked = Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.refreshToken(tokenDev1));
        Assertions.assertEquals(403, exRevoked.getStatusCode());

        // 4. Thiết bị 2 refresh bằng token 2 hợp lệ -> Thành công 200
        ApiResponse<AuthResponse.TokenData> refreshResp = service.refreshToken(tokenDev2);
        Assertions.assertEquals(200, refreshResp.getStatus());
        Assertions.assertNotNull(refreshResp.getData().getAccessToken());

        // 5. Thiết bị 2 logout -> Xóa token trong DB
        service.logout(tokenDev2);

        // 6. Sau logout, cố refresh lại -> Bị từ chối 403
        Assertions.assertThrows(
                AuthService.AuthException.class, () -> service.refreshToken(tokenDev2));
    }
}
