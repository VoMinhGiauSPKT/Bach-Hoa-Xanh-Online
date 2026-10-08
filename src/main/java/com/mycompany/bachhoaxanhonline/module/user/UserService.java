package com.mycompany.bachhoaxanhonline.module.user;

import com.mycompany.bachhoaxanhonline.module.auth.Auth;
import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static class UserException extends RuntimeException {
        private final int statusCode;

        public UserException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public UserResponse.ApiResponse<UserResponse.UserProfileData> getProfile(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserException(401, "Chưa đăng nhập, thiếu token hoặc token đã hết hạn / không hợp lệ");
        }

        Auth user = userRepository.findUserById(userId.trim())
                .orElseThrow(() -> new UserException(404, "Không tìm thấy thông tin tài khoản trong cơ sở dữ liệu"));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new UserException(403, "Tài khoản đã bị vô hiệu hóa hoặc xóa mềm");
        }

        boolean isEmployee = userRepository.isEmployee(userId.trim());

        UserResponse.UserProfileData profileData;
        if (isEmployee) {
            UserRepository.EmployeeInfo empInfo = userRepository.getEmployeeInfo(userId.trim());
            String position = empInfo != null ? empInfo.getPosition() : "STAFF";
            profileData = new UserResponse.UserProfileData(
                    user.getMaNguoiDung(),
                    user.getTenDangNhap(),
                    user.getTenND(),
                    user.getEmail(),
                    user.getSoDienThoai(),
                    user.getNgaySinh(),
                    "EMPLOYEE",
                    position,
                    empInfo != null ? empInfo.getHireDate() : null);
        } else {
            List<UserResponse.AddressData> addresses = userRepository.findAddressesByCustomerId(userId.trim());
            profileData = new UserResponse.UserProfileData(
                    user.getMaNguoiDung(),
                    user.getTenDangNhap(),
                    user.getTenND(),
                    user.getEmail(),
                    user.getSoDienThoai(),
                    user.getNgaySinh(),
                    "CUSTOMER",
                    addresses);
        }

        return new UserResponse.ApiResponse<>(200, "Lấy thông tin tài khoản thành công", profileData);
    }

    public UserResponse.ApiResponse<Void> changePassword(String userId, UserRequest.ChangePasswordRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserException(401, "Chưa đăng nhập hoặc token không hợp lệ / hết hạn");
        }

        if (request == null) {
            throw new UserException(400, "Dữ liệu gửi lên không đúng định dạng");
        }

        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            throw new UserException(400, "Mật khẩu hiện tại không được để trống");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new UserException(400, "Mật khẩu mới không được để trống");
        }
        if (request.getNewPassword().length() < 6) {
            throw new UserException(400, "Mật khẩu mới phải từ 6 ký tự trở lên");
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            throw new UserException(400, "Xác nhận mật khẩu không được để trống");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new UserException(400, "Mật khẩu mới không trùng khớp với confirmPassword");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new UserException(400, violation);
        }

        Auth user = userRepository.findUserById(userId.trim())
                .orElseThrow(() -> new UserException(404, "Không tìm thấy tài khoản người dùng trong cơ sở dữ liệu"));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new UserException(403, "Tài khoản đã bị khóa hoặc xóa mềm");
        }

        // 1. Kiểm tra mật khẩu hiện tại có đúng với CSDL hay không trước tiên
        if (!PasswordUtil.verify(request.getCurrentPassword(), user.getMatKhauHashed())) {
            throw new UserException(401, "Mật khẩu hiện tại không chính xác");
        }

        // 2. Khi mật khẩu hiện tại đã chính xác, mới kiểm tra mật khẩu mới có trùng mật khẩu cũ không
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new UserException(400, "Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        String hashedNewPassword = PasswordUtil.hash(request.getNewPassword());
        userRepository.updatePasswordAndRevokeToken(userId.trim(), hashedNewPassword);

        sendSecurityEmailAsync(user.getEmail(), user.getTenND());

        return new UserResponse.ApiResponse<>(200, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
    }

    // Gửi email cảnh báo bảo mật bất đồng bộ khi đổi mật khẩu thành công (mô phỏng)

    private void sendSecurityEmailAsync(String recipientEmail, String recipientName) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
            String message = String.format(
                    "[SECURITY ALERT] Gửi tới: %s (%s)%n" +
                            "Nội dung: Mật khẩu tài khoản Bách Hóa Xanh Online của bạn vừa được thay đổi thành công vào lúc %s.%n"
                            +
                            "Nếu không phải bạn thực hiện, vui lòng liên hệ ngay với chúng tôi để khóa tài khoản khẩn cấp.",
                    recipientName, recipientEmail, time);
            System.out.println(message);
        });
    }
}
