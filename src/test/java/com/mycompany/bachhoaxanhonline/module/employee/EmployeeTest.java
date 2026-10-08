package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.module.auth.AuthRequest;
import com.mycompany.bachhoaxanhonline.module.auth.AuthService;
import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import java.time.LocalDate;
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

    @Test
    public void testGetEmployeesPaginationAndFilter() {
        EmployeeService service = new EmployeeService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "filter_" + rand;
        String phone = "09" + String.format("%08d", rand);

        // Tạo nhân viên mẫu để tìm kiếm
        service.createEmployee(new EmployeeRequest.CreateEmployeeRequest(
                "Nguyen Filter Test", username, "Password123!", phone, "STAFF"));

        // 1. Lấy danh sách nhân viên không kèm bộ lọc
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeListData> respAll = service.getEmployees(1, 10, null, null, null);
        Assertions.assertEquals(200, respAll.getStatus());
        Assertions.assertNotNull(respAll.getData());
        Assertions.assertTrue(respAll.getData().getTotal() > 0);
        Assertions.assertNotNull(respAll.getData().getEmployees());

        // 2. Tìm kiếm theo keyword
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeListData> respKw = service.getEmployees(1, 10, username, null, null);
        Assertions.assertEquals(200, respKw.getStatus());
        Assertions.assertNotNull(respKw.getData());
        Assertions.assertTrue(respKw.getData().getTotal() >= 1);
        Assertions.assertEquals(username, respKw.getData().getEmployees().get(0).getUsername());

        // 3. Lọc theo chức vụ
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeListData> respStaff = service.getEmployees(1, 10, null, "STAFF", true);
        Assertions.assertEquals(200, respStaff.getStatus());
        Assertions.assertNotNull(respStaff.getData());
    }

    @Test
    public void testGetEmployeeByIdAndNotFound() {
        EmployeeService service = new EmployeeService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "detail_" + rand;
        String phone = "09" + String.format("%08d", rand);

        // Tạo nhân viên
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> created = service.createEmployee(
                new EmployeeRequest.CreateEmployeeRequest("Le Detail", username, "Password123!", phone, "STAFF"));
        String empId = created.getData().getEmployeeId();

        // 1. Lấy thông tin chi tiết thành công (200)
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeDetailData> detail = service.getEmployeeById(empId);
        Assertions.assertEquals(200, detail.getStatus());
        Assertions.assertNotNull(detail.getData());
        Assertions.assertEquals(empId, detail.getData().getEmployeeId());
        Assertions.assertEquals("Le Detail", detail.getData().getFullName());
        Assertions.assertEquals(username, detail.getData().getUsername());
        Assertions.assertEquals("STAFF", detail.getData().getPosition());
        Assertions.assertTrue(detail.getData().isStatus());

        // 2. Không tìm thấy ID -> 404 Not Found
        EmployeeService.EmployeeException exNotFound = Assertions.assertThrows(
                EmployeeService.EmployeeException.class, () -> service.getEmployeeById("non-existent-id"));
        Assertions.assertEquals(404, exNotFound.getStatusCode());
    }

    @Test
    public void testUpdateEmployeeAndConflict() {
        EmployeeService service = new EmployeeService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "update_" + rand;
        String phone = "09" + String.format("%08d", rand);
        String phoneOther = "08" + String.format("%08d", rand);

        // Tạo 2 nhân viên
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> emp1 = service.createEmployee(
                new EmployeeRequest.CreateEmployeeRequest("User One", username, "Password123!", phone, "STAFF"));
        service.createEmployee(
                new EmployeeRequest.CreateEmployeeRequest("User Two", "other_" + rand, "Password123!", phoneOther, "STAFF"));

        String empId1 = emp1.getData().getEmployeeId();

        // 1. Cập nhật thành công thông tin và chức vụ lên ADMIN
        EmployeeRequest.UpdateEmployeeRequest updateReq = new EmployeeRequest.UpdateEmployeeRequest(
                "User One Updated", "user1_" + rand + "@test.com", "09" + String.format("%08d", rand + 1),
                LocalDate.of(1996, 4, 15), "ADMIN", "NewPassword123!");
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> updateResp = service.updateEmployee(empId1, updateReq);
        Assertions.assertEquals(200, updateResp.getStatus());
        Assertions.assertEquals("User One Updated", updateResp.getData().getFullName());
        Assertions.assertEquals("ADMIN", updateResp.getData().getPosition());

        // 2. Cập nhật trùng số điện thoại với nhân viên khác -> 409 Conflict
        EmployeeRequest.UpdateEmployeeRequest dupPhoneReq = new EmployeeRequest.UpdateEmployeeRequest(
                "User One", null, phoneOther, null, null, null);
        EmployeeService.EmployeeException exConflict = Assertions.assertThrows(
                EmployeeService.EmployeeException.class, () -> service.updateEmployee(empId1, dupPhoneReq));
        Assertions.assertEquals(409, exConflict.getStatusCode());

        // 3. Không tìm thấy ID cần cập nhật -> 404 Not Found
        EmployeeService.EmployeeException ex404 = Assertions.assertThrows(
                EmployeeService.EmployeeException.class, () -> service.updateEmployee("unknown-id", updateReq));
        Assertions.assertEquals(404, ex404.getStatusCode());
    }

    @Test
    public void testUpdateStatusAndSelfBlockProtection() {
        EmployeeService service = new EmployeeService();
        long rand = System.currentTimeMillis() % 1000000;
        String username = "status_" + rand;
        String phone = "09" + String.format("%08d", rand);

        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> created = service.createEmployee(
                new EmployeeRequest.CreateEmployeeRequest("Status User", username, "Password123!", phone, "STAFF"));
        String empId = created.getData().getEmployeeId();

        // 1. Admin tự khóa chính mình -> 403 Forbidden
        EmployeeService.EmployeeException exForbidden = Assertions.assertThrows(
                EmployeeService.EmployeeException.class,
                () -> service.updateStatus(empId, new EmployeeRequest.UpdateStatusRequest(false), empId));
        Assertions.assertEquals(403, exForbidden.getStatusCode());

        // 2. Khóa tài khoản thành công (status = false)
        EmployeeResponse.ApiResponse<EmployeeResponse.UpdateStatusData> patchResp = service.updateStatus(
                empId, new EmployeeRequest.UpdateStatusRequest(false), "admin-super-id");
        Assertions.assertEquals(200, patchResp.getStatus());
        Assertions.assertFalse(patchResp.getData().isStatus());

        // Kiểm tra lại chi tiết thấy status = false
        EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeDetailData> detail = service.getEmployeeById(empId);
        Assertions.assertFalse(detail.getData().isStatus());

        // 3. Kích hoạt lại tài khoản thành công (status = true)
        EmployeeResponse.ApiResponse<EmployeeResponse.UpdateStatusData> patchResp2 = service.updateStatus(
                empId, new EmployeeRequest.UpdateStatusRequest(true), "admin-super-id");
        Assertions.assertEquals(200, patchResp2.getStatus());
        Assertions.assertTrue(patchResp2.getData().isStatus());
    }
}
