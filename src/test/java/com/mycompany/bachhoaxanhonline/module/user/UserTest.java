package com.mycompany.bachhoaxanhonline.module.user;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTest {

    private final UserService userService = new UserService();

    @Test
    public void testCustomerGetProfileSuccess() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        long rand = System.currentTimeMillis() % 1000000;
        String customerId = "kh_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String username = "customer_" + rand;
        String phone = "09" + String.format("%08d", rand);
        String fullName = "Nguyen Van Khach Hang Test";
        String email = "kh_" + rand + "@gmail.com";

        try {
            tx.begin();

            Auth user = new Auth();
            user.setMaNguoiDung(customerId);
            user.setTenND(fullName);
            user.setTenDangNhap(username);
            user.setEmail(email);
            user.setSoDienThoai(phone);
            user.setMatKhauHashed(PasswordUtil.hash("Password123!"));
            user.setNgaySinh(LocalDate.of(2000, 1, 1));
            user.setDeleted(false);

            Customer customer = new Customer();
            customer.setUser(user);
            customer.setDeleted(false);
            em.persist(customer);

            // Thêm địa chỉ thử nghiệm vào bảng DiaChi
            em.createNativeQuery(
                    "INSERT INTO \"DiaChi\" (\"maKhachHang\", \"tenNguoiNhan\", \"soDienThoai\", \"soNha\", \"phuong\", \"tinh\", \"laMacDinh\", \"Deleted\") " +
                    "VALUES (:maKh, :ten, :sdt, :soNha, :phuong, :tinh, true, false)")
                    .setParameter("maKh", customerId)
                    .setParameter("ten", fullName)
                    .setParameter("sdt", phone)
                    .setParameter("soNha", "123 Đường Số 1")
                    .setParameter("phuong", "Phường Linh Trung")
                    .setParameter("tinh", "TP. Thủ Đức")
                    .executeUpdate();

            tx.commit();

            // Gọi service getProfile
            ApiResponse<UserResponse.UserProfileData> resp = userService.getProfile(customerId);

            Assertions.assertEquals(200, resp.getStatus());
            Assertions.assertEquals("Lấy thông tin tài khoản thành công", resp.getMessage());
            Assertions.assertNotNull(resp.getData());
            Assertions.assertEquals(customerId, resp.getData().getUserId());
            Assertions.assertEquals(username, resp.getData().getUsername());
            Assertions.assertEquals(fullName, resp.getData().getFullName());
            Assertions.assertEquals(email, resp.getData().getEmail());
            Assertions.assertEquals(phone, resp.getData().getPhoneNumber());
            Assertions.assertEquals("CUSTOMER", resp.getData().getUserType());
            Assertions.assertNull(resp.getData().getPosition());
            Assertions.assertNull(resp.getData().getHireDate());

            // Kiểm tra danh sách địa chỉ
            Assertions.assertNotNull(resp.getData().getAddresses());
            Assertions.assertEquals(1, resp.getData().getAddresses().size());
            UserResponse.AddressData addr = resp.getData().getAddresses().get(0);
            Assertions.assertEquals(fullName, addr.getReceiverName());
            Assertions.assertEquals("123 Đường Số 1", addr.getStreet());
            Assertions.assertEquals("Phường Linh Trung", addr.getWard());
            Assertions.assertEquals("TP. Thủ Đức", addr.getCity());
            Assertions.assertTrue(addr.getIsDefault());

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void testEmployeeGetProfileSuccess() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        long rand = System.currentTimeMillis() % 1000000;
        String employeeId = "emp_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String username = "staff_test_" + rand;
        String phone = "08" + String.format("%08d", rand);
        String fullName = "Le Van Nhan Vien Test";
        String email = "staff_" + rand + "@bachhoaxanh.test";

        try {
            tx.begin();

            Auth user = new Auth();
            user.setMaNguoiDung(employeeId);
            user.setTenND(fullName);
            user.setTenDangNhap(username);
            user.setEmail(email);
            user.setSoDienThoai(phone);
            user.setMatKhauHashed(PasswordUtil.hash("Password123!"));
            user.setNgaySinh(LocalDate.of(1995, 5, 20));
            user.setDeleted(false);

            Employee employee = new Employee();
            employee.setUser(user);
            employee.setChucVu(EmployeeRole.STAFF);
            employee.setNgayVaoLam(LocalDate.of(2026, 2, 15));
            employee.setDeleted(false);
            em.persist(employee);

            tx.commit();

            // Gọi service getProfile
            ApiResponse<UserResponse.UserProfileData> resp = userService.getProfile(employeeId);

            Assertions.assertEquals(200, resp.getStatus());
            Assertions.assertNotNull(resp.getData());
            Assertions.assertEquals(employeeId, resp.getData().getUserId());
            Assertions.assertEquals("EMPLOYEE", resp.getData().getUserType());
            Assertions.assertEquals("STAFF", resp.getData().getPosition());
            Assertions.assertEquals(LocalDate.of(2026, 2, 15), resp.getData().getHireDate());
            Assertions.assertNull(resp.getData().getAddresses());

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void testGetProfileNotFound() {
        UserService.UserException ex = Assertions.assertThrows(
                UserService.UserException.class, () -> userService.getProfile("non_existent_user_id_12345"));
        Assertions.assertEquals(404, ex.getStatusCode());
        Assertions.assertTrue(ex.getMessage().contains("Không tìm thấy"));
    }

    @Test
    public void testChangePasswordSuccessAndValidationErrors() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        long rand = System.currentTimeMillis() % 1000000;
        String userId = "user_pwd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String username = "user_pwd_" + rand;
        String phone = "07" + String.format("%08d", rand);
        String oldPassword = "Password123!";

        try {
            tx.begin();

            Auth user = new Auth();
            user.setMaNguoiDung(userId);
            user.setTenND("User Pwd Test");
            user.setTenDangNhap(username);
            user.setSoDienThoai(phone);
            user.setMatKhauHashed(PasswordUtil.hash(oldPassword));
            user.setDeleted(false);
            em.persist(user);

            tx.commit();

            // 1. Lỗi thiếu thông tin bắt buộc -> 400 Bad Request
            UserRequest.ChangePasswordRequest reqMissing = new UserRequest.ChangePasswordRequest(
                    null, "NewPassword456!", "NewPassword456!");
            UserService.UserException exMissing = Assertions.assertThrows(
                    UserService.UserException.class, () -> userService.changePassword(userId, reqMissing));
            Assertions.assertEquals(400, exMissing.getStatusCode());

            // 2. Lỗi mật khẩu mới < 6 ký tự -> 400 Bad Request
            UserRequest.ChangePasswordRequest reqShort = new UserRequest.ChangePasswordRequest(
                    oldPassword, "123", "123");
            UserService.UserException exShort = Assertions.assertThrows(
                    UserService.UserException.class, () -> userService.changePassword(userId, reqShort));
            Assertions.assertEquals(400, exShort.getStatusCode());

            // 3. Lỗi mật khẩu xác nhận không khớp -> 400 Bad Request
            UserRequest.ChangePasswordRequest reqMismatch = new UserRequest.ChangePasswordRequest(
                    oldPassword, "NewPassword456!", "DifferentPassword789!");
            UserService.UserException exMismatch = Assertions.assertThrows(
                    UserService.UserException.class, () -> userService.changePassword(userId, reqMismatch));
            Assertions.assertEquals(400, exMismatch.getStatusCode());

            // 4. Lỗi mật khẩu mới trùng mật khẩu cũ -> 400 Bad Request
            UserRequest.ChangePasswordRequest reqSame = new UserRequest.ChangePasswordRequest(
                    oldPassword, oldPassword, oldPassword);
            UserService.UserException exSame = Assertions.assertThrows(
                    UserService.UserException.class, () -> userService.changePassword(userId, reqSame));
            Assertions.assertEquals(400, exSame.getStatusCode());

            // 5. Lỗi mật khẩu hiện tại không chính xác -> 401 Unauthorized
            UserRequest.ChangePasswordRequest reqWrongOld = new UserRequest.ChangePasswordRequest(
                    "WrongPassword999!", "NewPassword456!", "NewPassword456!");
            UserService.UserException exWrongOld = Assertions.assertThrows(
                    UserService.UserException.class, () -> userService.changePassword(userId, reqWrongOld));
            Assertions.assertEquals(401, exWrongOld.getStatusCode());

            // 6. Nhập mật khẩu cũ sai và mật khẩu mới trùng mật khẩu sai đó -> Ưu tiên báo 401 Unauthorized
            UserRequest.ChangePasswordRequest reqWrongOldAndSame = new UserRequest.ChangePasswordRequest(
                    "WrongPassword999!", "WrongPassword999!", "WrongPassword999!");
            UserService.UserException exWrongOldSame = Assertions.assertThrows(
                    UserService.UserException.class, () -> userService.changePassword(userId, reqWrongOldAndSame));
            Assertions.assertEquals(401, exWrongOldSame.getStatusCode());
            Assertions.assertEquals("Mật khẩu hiện tại không chính xác", exWrongOldSame.getMessage());

            // 7. Đổi mật khẩu thành công -> 200 OK
            UserRequest.ChangePasswordRequest reqSuccess = new UserRequest.ChangePasswordRequest(
                    oldPassword, "NewPassword456!", "NewPassword456!");
            ApiResponse<Void> resp = userService.changePassword(userId, reqSuccess);

            Assertions.assertEquals(200, resp.getStatus());
            Assertions.assertEquals("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", resp.getMessage());

            // Kiểm tra trong CSDL: Mật khẩu mới xác thực thành công, mật khẩu cũ thất bại
            em.clear();
            Auth updatedUser = em.find(Auth.class, userId);
            Assertions.assertNotNull(updatedUser);
            Assertions.assertTrue(PasswordUtil.verify("NewPassword456!", updatedUser.getMatKhauHashed()));
            Assertions.assertFalse(PasswordUtil.verify(oldPassword, updatedUser.getMatKhauHashed()));
            Assertions.assertNull(updatedUser.getRefreshToken()); // Đã bị thu hồi phiên

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
