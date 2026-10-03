package com.mycompany.bachhoaxanhonline.auth;

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

    // DTO bọc kết quả đăng nhập gồm LoginResponse (gửi cho client) và refreshToken (để lưu vào HttpOnly Cookie)
    public static class LoginResult {
        private final AuthResponse.LoginResponse response;
        private final String refreshToken;

        public LoginResult(AuthResponse.LoginResponse response, String refreshToken) {
            this.response = response;
            this.refreshToken = refreshToken;
        }

        public AuthResponse.LoginResponse getResponse() {
            return response;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    // Exception tùy biến cho nghiệp vụ xác thực
    public static class AuthException extends RuntimeException {
        private final int statusCode;
        private final String errorCode;

        public AuthException(int statusCode, String errorCode, String message) {
            super(message);
            this.statusCode = statusCode;
            this.errorCode = errorCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public AuthResponse.RegisterResponse registerCustomer(AuthRequest.RegisterRequest request) {
        // 1. Kiểm tra validation
        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new AuthException(400, "BAD_REQUEST", violation);
        }

        // 2. Kiểm tra số điện thoại đã tồn tại chưa
        if (authRepository.existsByPhone(request.getSoDienThoai())) {
            throw new AuthException(409, "PHONE_EXISTS", "Số điện thoại đã tồn tại");
        }

        // 3. Mã hóa mật khẩu
        String hashedPass = PasswordUtil.hash(request.getPassword());

        // 4. Sinh mã người dùng và lưu
        String userId = "ND_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Auth auth = new Auth(userId, request.getTenND(), request.getSoDienThoai(), hashedPass, request.getNgaySinh());

        authRepository.registerCustomer(auth);

        return new AuthResponse.RegisterResponse(userId);
    }

    public LoginResult loginCustomer(AuthRequest.LoginRequest request) {
        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new AuthException(400, "BAD_REQUEST", violation);
        }

        Optional<Auth> optAuth = authRepository.findByPhone(request.getSoDienThoai());
        if (optAuth.isEmpty() || !PasswordUtil.verify(request.getPassword(), optAuth.get().getMatKhauHashed())) {
            throw new AuthException(401, "UNAUTHORIZED", "Sai thông tin đăng nhập");
        }

        Auth auth = optAuth.get();

        if (Boolean.TRUE.equals(auth.getDeleted())) {
            throw new AuthException(403, "ACCOUNT_LOCKED", "Tài khoản bị khóa");
        }

        // Cấp Access Token (1h) và Refresh Token (7 ngày)
        String role = "CUSTOMER";
        String accessToken = JwtUtil.generateAccessToken(auth.getMaNguoiDung(), auth.getTenND(), role);
        String refreshToken = JwtUtil.generateRefreshToken(auth.getMaNguoiDung());

        // LƯU VÀO DATABASE: Ghi đè token mới, tự động vô hiệu hóa phiên ở các thiết bị cũ (Single Device)
        authRepository.updateRefreshToken(auth.getMaNguoiDung(), refreshToken);

        AuthResponse.UserDto userDto = new AuthResponse.UserDto(auth.getMaNguoiDung(), auth.getTenND(), role);
        AuthResponse.LoginResponse response = new AuthResponse.LoginResponse(accessToken, refreshToken, userDto);

        return new LoginResult(response, refreshToken);
    }

    public LoginResult loginAdmin(AuthRequest.LoginRequest request) {
        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new AuthException(400, "BAD_REQUEST", violation);
        }

        Optional<Auth> optAuth = authRepository.findByPhone(request.getSoDienThoai());
        if (optAuth.isEmpty() || !PasswordUtil.verify(request.getPassword(), optAuth.get().getMatKhauHashed())) {
            throw new AuthException(401, "UNAUTHORIZED", "Sai tài khoản/mật khẩu");
        }

        Auth auth = optAuth.get();

        // Kiểm tra xem tài khoản có tồn tại trong bảng NhanVien không
        if (!authRepository.isStaff(auth.getMaNguoiDung())) {
            throw new AuthException(403, "FORBIDDEN", "Không phải tài khoản nhân viên");
        }

        if (Boolean.TRUE.equals(auth.getDeleted())) {
            throw new AuthException(403, "ACCOUNT_LOCKED", "Tài khoản bị khóa");
        }

        String role = "STAFF";
        String accessToken = JwtUtil.generateAccessToken(auth.getMaNguoiDung(), auth.getTenND(), role);
        String refreshToken = JwtUtil.generateRefreshToken(auth.getMaNguoiDung());

        // LƯU VÀO DATABASE: Ghi đè token mới
        authRepository.updateRefreshToken(auth.getMaNguoiDung(), refreshToken);

        AuthResponse.UserDto userDto = new AuthResponse.UserDto(auth.getMaNguoiDung(), auth.getTenND(), role);
        AuthResponse.LoginResponse response = new AuthResponse.LoginResponse(accessToken, refreshToken, userDto);

        return new LoginResult(response, refreshToken);
    }

    public AuthResponse.TokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty() || !JwtUtil.validateToken(refreshToken)) {
            throw new AuthException(401, "TOKEN_EXPIRED", "Token hết hạn / không hợp lệ");
        }

        String userId;
        try {
            userId = JwtUtil.getUserIdFromToken(refreshToken);
        } catch (Exception e) {
            throw new AuthException(401, "INVALID_TOKEN", "Token không hợp lệ");
        }

        Optional<Auth> optAuth = authRepository.findById(userId);
        if (optAuth.isEmpty() || Boolean.TRUE.equals(optAuth.get().getDeleted())) {
            throw new AuthException(401, "UNAUTHORIZED", "Người dùng không tồn tại hoặc đã bị khóa");
        }

        Auth auth = optAuth.get();

        // ĐỐI CHIẾU VỚI DATABASE: Kiểm tra xem token gửi lên có khớp với token đang lưu trong DB không
        if (auth.getRefreshToken() == null || !auth.getRefreshToken().equals(refreshToken)) {
            throw new AuthException(401, "TOKEN_REVOKED", "Phiên đăng nhập đã hết hạn hoặc tài khoản đã đăng nhập ở thiết bị khác");
        }

        String role = authRepository.isStaff(userId) ? "STAFF" : "CUSTOMER";

        String newAccessToken = JwtUtil.generateAccessToken(auth.getMaNguoiDung(), auth.getTenND(), role);
        return new AuthResponse.TokenResponse(newAccessToken);
    }

    public void logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new AuthException(401, "INVALID_TOKEN", "Token không hợp lệ");
        }

        try {
            String userId = JwtUtil.getUserIdFromToken(token);
            // XÓA TRONG DATABASE: Đặt refreshToken = null để vô hiệu hóa hoàn toàn
            authRepository.updateRefreshToken(userId, null);
        } catch (Exception e) {
            throw new AuthException(401, "INVALID_TOKEN", "Token không hợp lệ");
        }
    }
}
