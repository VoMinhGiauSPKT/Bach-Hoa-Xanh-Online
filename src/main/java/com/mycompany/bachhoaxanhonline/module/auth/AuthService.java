package com.mycompany.bachhoaxanhonline.module.auth;

import com.mycompany.bachhoaxanhonline.util.JwtUtil;
import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private final AuthRepository authRepository;

    public AuthService() {
        this.authRepository = new AuthRepository();
    }

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public static class LoginResult {
        private final AuthResponse.ApiResponse<AuthResponse.LoginData> response;
        private final String refreshToken;

        public LoginResult(AuthResponse.ApiResponse<AuthResponse.LoginData> response, String refreshToken) {
            this.response = response;
            this.refreshToken = refreshToken;
        }

        public AuthResponse.ApiResponse<AuthResponse.LoginData> getResponse() {
            return response;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    public static class AuthException extends RuntimeException {
        private final int statusCode;

        public AuthException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public AuthResponse.ApiResponse<AuthResponse.RegisterData> registerCustomer(AuthRequest.RegisterRequest request) {
        // 1. Kiểm tra thông tin bắt buộc
        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new AuthException(400, "Thiếu thông tin bắt buộc: " + violation);
        }

        // 2. Kiểm tra trùng lặp
        if (authRepository.existsByUsername(request.getUsername())) {
            throw new AuthException(409, "Tên đăng nhập đã tồn tại trong hệ thống");
        }
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(409, "Email đã tồn tại trong hệ thống");
        }
        if (authRepository.existsByPhone(request.getPhoneNumber())) {
            throw new AuthException(409, "Số điện thoại đã tồn tại trong hệ thống");
        }

        // 3. Băm mật khẩu bằng BCrypt
        String hashedPass = PasswordUtil.hash(request.getPassword());

        // 4. Sinh UUID cho maNguoiDung
        String customerId = UUID.randomUUID().toString();
        Auth auth = new Auth(
                customerId,
                request.getFullName(),
                request.getUsername(),
                request.getEmail(),
                request.getPhoneNumber(),
                hashedPass,
                request.getBirthDate()
        );

        // 5. Lưu vào CSDL
        authRepository.registerCustomer(auth);

        // 6. Trả về kết quả (tuyệt đối không trả về password)
        AuthResponse.RegisterData data = new AuthResponse.RegisterData(
                customerId,
                request.getUsername(),
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        return new AuthResponse.ApiResponse<>(201, "Đăng ký thành công", data);
    }

    public LoginResult login(AuthRequest.LoginRequest request) {
        // 1. Kiểm tra thông tin bắt buộc
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AuthException(400, "Thiếu thông tin đăng nhập");
        }

        // 2. Tìm kiếm duy nhất bằng tên đăng nhập (tenDangNhap)
        Optional<Auth> optAuth = authRepository.findByUsername(request.getUsername().trim());
        if (optAuth.isEmpty() || !PasswordUtil.verify(request.getPassword(), optAuth.get().getMatKhauHashed())) {
            throw new AuthException(401, "Tài khoản hoặc mật khẩu không chính xác");
        }

        Auth auth = optAuth.get();

        // 3. Kiểm tra tài khoản bị khóa hoặc xóa mềm
        if (Boolean.TRUE.equals(auth.getDeleted())) {
            throw new AuthException(403, "Tài khoản đã bị khóa hoặc xóa mềm");
        }

        // 4. Phân biệt loại tài khoản: Nhân viên hay Khách hàng
        String position = authRepository.getEmployeePosition(auth.getMaNguoiDung());
        String role;
        AuthResponse.LoginData loginData;

        if (position != null) {
            // Là Nhân viên (ADMIN hoặc STAFF)
            role = position;
            String accessToken = JwtUtil.generateAccessToken(auth.getMaNguoiDung(), auth.getTenND(), role);
            AuthResponse.EmployeeInfo empInfo = new AuthResponse.EmployeeInfo(
                    auth.getMaNguoiDung(),
                    auth.getTenDangNhap(),
                    auth.getTenND(),
                    position
            );
            loginData = AuthResponse.LoginData.forEmployee(accessToken, empInfo);
        } else {
            // Là Khách hàng
            role = "CUSTOMER";
            String accessToken = JwtUtil.generateAccessToken(auth.getMaNguoiDung(), auth.getTenND(), role);
            AuthResponse.CustomerInfo custInfo = new AuthResponse.CustomerInfo(
                    auth.getMaNguoiDung(),
                    auth.getTenDangNhap(),
                    auth.getTenND()
            );
            loginData = AuthResponse.LoginData.forCustomer(accessToken, custInfo);
        }

        // 5. Cấp Refresh Token và cập nhật vào CSDL (Single-Device Session)
        String refreshToken = JwtUtil.generateRefreshToken(auth.getMaNguoiDung());
        authRepository.updateRefreshToken(auth.getMaNguoiDung(), refreshToken);

        AuthResponse.ApiResponse<AuthResponse.LoginData> response = new AuthResponse.ApiResponse<>(
                200, "Đăng nhập thành công", loginData);

        return new LoginResult(response, refreshToken);
    }

    public AuthResponse.ApiResponse<AuthResponse.TokenData> refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new AuthException(401, "Không tìm thấy token trong cookie");
        }

        if (!JwtUtil.validateToken(refreshToken)) {
            throw new AuthException(403, "RefreshToken hết hạn / không hợp lệ / không khớp với cột refreshToken trong database");
        }

        String userId;
        try {
            userId = JwtUtil.getUserIdFromToken(refreshToken);
        } catch (Exception e) {
            throw new AuthException(403, "RefreshToken hết hạn / không hợp lệ / không khớp với cột refreshToken trong database");
        }

        Optional<Auth> optAuth = authRepository.findById(userId);
        if (optAuth.isEmpty() || Boolean.TRUE.equals(optAuth.get().getDeleted())) {
            throw new AuthException(403, "RefreshToken hết hạn / không hợp lệ / không khớp với cột refreshToken trong database");
        }

        Auth auth = optAuth.get();
        if (auth.getRefreshToken() == null || !auth.getRefreshToken().equals(refreshToken)) {
            throw new AuthException(403, "RefreshToken hết hạn / không hợp lệ / không khớp với cột refreshToken trong database");
        }

        String position = authRepository.getEmployeePosition(userId);
        String role = (position != null) ? position : "CUSTOMER";

        String newAccessToken = JwtUtil.generateAccessToken(auth.getMaNguoiDung(), auth.getTenND(), role);
        return new AuthResponse.ApiResponse<>(200, new AuthResponse.TokenData(newAccessToken));
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            try {
                if (JwtUtil.validateToken(refreshToken)) {
                    String userId = JwtUtil.getUserIdFromToken(refreshToken);
                    authRepository.updateRefreshToken(userId, null);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
