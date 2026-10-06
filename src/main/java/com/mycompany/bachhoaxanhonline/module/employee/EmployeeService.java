package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.util.UUID;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService() {
        this.employeeRepository = new EmployeeRepository();
    }

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public static class EmployeeException extends RuntimeException {
        private final int statusCode;

        public EmployeeException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> createEmployee(EmployeeRequest.CreateEmployeeRequest request) {
        // 1. Kiểm tra thông tin bắt buộc
        if (request == null) {
            throw new EmployeeException(400, "Thiếu thông tin bắt buộc");
        }
        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new EmployeeException(400, "Thiếu thông tin bắt buộc: " + violation);
        }

        // 2. Chuẩn hóa và kiểm tra chức vụ (position)
        String position = request.getPosition();
        if (position == null || position.trim().isEmpty()) {
            position = "STAFF";
        } else {
            position = position.trim().toUpperCase();
        }
        if (!"ADMIN".equals(position) && !"STAFF".equals(position)) {
            throw new EmployeeException(400, "Chức vụ không hợp lệ, phải là ADMIN hoặc STAFF");
        }

        // 3. Kiểm tra trùng lặp tên đăng nhập hoặc số điện thoại
        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new EmployeeException(409, "Tên đăng nhập đã tồn tại");
        }
        if (employeeRepository.existsByPhone(request.getPhoneNumber())) {
            throw new EmployeeException(409, "Số điện thoại đã tồn tại");
        }

        // 4. Băm mật khẩu bằng BCrypt
        String hashedPassword = PasswordUtil.hash(request.getPassword());

        // 5. Tự sinh UUID cho employeeId
        String employeeId = UUID.randomUUID().toString();

        // 6. Thêm bản ghi vào bảng NguoiDung và NhanVien
        employeeRepository.createEmployee(
                employeeId,
                request.getFullName(),
                request.getUsername(),
                request.getPhoneNumber(),
                hashedPassword,
                position
        );

        // 7. Trả về kết quả (tuyệt đối không trả về mật khẩu)
        EmployeeResponse.EmployeeData data = new EmployeeResponse.EmployeeData(
                employeeId,
                request.getFullName(),
                request.getUsername(),
                request.getPhoneNumber(),
                position,
                true
        );

        return new EmployeeResponse.ApiResponse<>(201, "Tạo nhân viên thành công", data);
    }
}
