package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.module.auth.AuthRequest;
import com.mycompany.bachhoaxanhonline.module.auth.AuthService;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmployeeTest {

    @Test
    public void testCreateEmployeeSuccessAndConflict() {
        EmployeeService service = new EmployeeService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "staff_" + rand;
        String phone = "09" + String.format("%08d", rand);

        // 1. Tạo nhân viên STAFF thành công (201)
        EmployeeRequest.CreateEmployeeRequest req = new EmployeeRequest.CreateEmployeeRequest(
                "Nguyen Van B", username, "Password123!", phone, "STAFF");
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> resp = service.createEmployee(req);

        Assertions.assertEquals(201, resp.getStatus());
        Assertions.assertEquals("Tạo nhân viên thành công", resp.getMessage());
        Assertions.assertNotNull(resp.getData());
        Assertions.assertEquals(username, resp.getData().getUsername());
        Assertions.assertEquals(phone, resp.getData().getPhoneNumber());
        Assertions.assertEquals("STAFF", resp.getData().getPosition());
        Assertions.assertTrue(resp.getData().isStatus());
        Assertions.assertNotNull(resp.getData().getEmployeeId());

        // 2. Trùng username -> 409 Conflict
        EmployeeRequest.CreateEmployeeRequest dupUserReq = new EmployeeRequest.CreateEmployeeRequest(
                "Nguyen Van C", username, "Password123!", "08" + String.format("%08d", rand), "STAFF");
        EmployeeService.EmployeeException exUser = Assertions.assertThrows(
                EmployeeService.EmployeeException.class, () -> service.createEmployee(dupUserReq));
        Assertions.assertEquals(409, exUser.getStatusCode());

        // 3. Trùng số điện thoại -> 409 Conflict
        EmployeeRequest.CreateEmployeeRequest dupPhoneReq = new EmployeeRequest.CreateEmployeeRequest(
                "Nguyen Van D", "staff2_" + rand, "Password123!", phone, "STAFF");
        EmployeeService.EmployeeException exPhone = Assertions.assertThrows(
                EmployeeService.EmployeeException.class, () -> service.createEmployee(dupPhoneReq));
        Assertions.assertEquals(409, exPhone.getStatusCode());

        // 4. Thiếu thông tin -> 400 Bad Request
        EmployeeRequest.CreateEmployeeRequest missingReq = new EmployeeRequest.CreateEmployeeRequest(
                null, "staff3_" + rand, "Password123!", "07" + String.format("%08d", rand), "STAFF");
        EmployeeService.EmployeeException exMissing = Assertions.assertThrows(
                EmployeeService.EmployeeException.class, () -> service.createEmployee(missingReq));
        Assertions.assertEquals(400, exMissing.getStatusCode());
    }

    @Test
    public void testLoginWithCreatedEmployee() {
        EmployeeService empService = new EmployeeService();
        AuthService authService = new AuthService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "admin_" + rand;
        String phone = "09" + String.format("%08d", rand);
        String pass = "Password123!";

        // Tạo nhân viên với chức vụ ADMIN
        empService.createEmployee(new EmployeeRequest.CreateEmployeeRequest(
                "Admin System", username, pass, phone, "ADMIN"));

        // Đăng nhập bằng tài khoản ADMIN mới tạo qua AuthService
        AuthRequest.LoginRequest loginReq = new AuthRequest.LoginRequest(username, pass);
        AuthService.LoginResult loginResult = authService.login(loginReq);

        Assertions.assertEquals(200, loginResult.getResponse().getStatus());
        Assertions.assertEquals("EMPLOYEE", loginResult.getResponse().getData().getUserType());
        Assertions.assertNotNull(loginResult.getResponse().getData().getEmployee());
        Assertions.assertEquals("ADMIN", loginResult.getResponse().getData().getEmployee().getPosition());
        Assertions.assertEquals(username, loginResult.getResponse().getData().getEmployee().getUsername());

        // Kiểm tra Access Token có claim role là "ADMIN"
        String accessToken = loginResult.getResponse().getData().getAccessToken();
        Assertions.assertNotNull(accessToken);
        Assertions.assertEquals("ADMIN", JwtUtil.getRoleFromToken(accessToken));
    }
}
