package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.module.auth.Auth;
import com.mycompany.bachhoaxanhonline.util.PasswordUtil;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> createEmployee(
            EmployeeRequest.CreateEmployeeRequest request) {
        if (request == null) {
            throw new EmployeeException(400, "Thiếu thông tin bắt buộc");
        }
        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new EmployeeException(400, "Thiếu thông tin bắt buộc: " + violation);
        }

        String position = request.getPosition();
        if (position == null || position.trim().isEmpty()) {
            position = "STAFF";
        } else {
            position = position.trim().toUpperCase();
        }
        if (!"ADMIN".equals(position) && !"STAFF".equals(position)) {
            throw new EmployeeException(400, "Chức vụ không hợp lệ, phải là ADMIN hoặc STAFF");
        }

        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new EmployeeException(409, "Tên đăng nhập đã tồn tại");
        }
        if (employeeRepository.existsByPhone(request.getPhoneNumber())) {
            throw new EmployeeException(409, "Số điện thoại đã tồn tại");
        }

        String hashedPassword = PasswordUtil.hash(request.getPassword());

        String employeeId = UUID.randomUUID().toString();

        employeeRepository.createEmployee(
                employeeId,
                request.getFullName(),
                request.getUsername(),
                request.getPhoneNumber(),
                hashedPassword,
                position);

        EmployeeResponse.EmployeeData data = new EmployeeResponse.EmployeeData(
                employeeId,
                request.getFullName(),
                request.getUsername(),
                request.getPhoneNumber(),
                position,
                true);

        return new EmployeeResponse.ApiResponse<>(201, "Tạo nhân viên thành công", data);
    }

    public EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeListData> getEmployees(
            int page, int limit, String keyword, String position, Boolean status) {
        int validPage = page > 0 ? page : 1;
        int validLimit = (limit > 0 && limit <= 100) ? limit : 10;
        String validKw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String validPos = (position != null && !position.trim().isEmpty()) ? position.trim().toUpperCase() : null;

        if (validPos != null && !"ADMIN".equals(validPos) && !"STAFF".equals(validPos)) {
            validPos = null;
        }

        long total = employeeRepository.countEmployees(validKw, validPos, status);
        List<Employee> list = employeeRepository.findEmployees(validPage, validLimit, validKw, validPos, status);

        List<EmployeeResponse.EmployeeListItem> items = new ArrayList<>();
        for (Employee emp : list) {
            items.add(new EmployeeResponse.EmployeeListItem(
                    emp.getMaNhanVien(),
                    emp.getFullName(),
                    emp.getUsername(),
                    emp.getEmail(),
                    emp.getPhoneNumber(),
                    emp.getChucVu() != null ? emp.getChucVu().name() : "STAFF",
                    emp.getNgayVaoLam(),
                    emp.isActive()));
        }

        EmployeeResponse.EmployeeListData data = new EmployeeResponse.EmployeeListData(total, validPage, validLimit,
                items);
        return new EmployeeResponse.ApiResponse<>(200, "Lấy danh sách nhân viên thành công", data);
    }

    public EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeDetailData> getEmployeeById(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new EmployeeException(400, "Thiếu ID nhân viên");
        }

        Optional<Employee> opt = employeeRepository.findById(employeeId.trim());
        if (opt.isEmpty()) {
            throw new EmployeeException(404, "Không tìm thấy nhân viên với ID tương ứng");
        }

        Employee emp = opt.get();
        Auth user = emp.getUser();
        EmployeeResponse.EmployeeDetailData data = new EmployeeResponse.EmployeeDetailData(
                emp.getMaNhanVien(),
                emp.getFullName(),
                emp.getUsername(),
                emp.getEmail(),
                emp.getPhoneNumber(),
                user != null ? user.getNgaySinh() : null,
                emp.getChucVu() != null ? emp.getChucVu().name() : "STAFF",
                emp.getNgayVaoLam(),
                emp.isActive());

        return new EmployeeResponse.ApiResponse<>(200, "Lấy thông tin nhân viên thành công", data);
    }

    public EmployeeResponse.ApiResponse<EmployeeResponse.EmployeeData> updateEmployee(
            String employeeId, EmployeeRequest.UpdateEmployeeRequest request) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new EmployeeException(400, "Thiếu ID nhân viên");
        }
        if (request == null) {
            throw new EmployeeException(400, "Thiếu dữ liệu cập nhật");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new EmployeeException(400, "Dữ liệu gửi lên không đúng định dạng: " + violation);
        }

        Optional<Employee> opt = employeeRepository.findById(employeeId.trim());
        if (opt.isEmpty()) {
            throw new EmployeeException(404, "Không tìm thấy nhân viên cần cập nhật");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (employeeRepository.existsByEmailAndNotId(request.getEmail().trim(), employeeId.trim())) {
                throw new EmployeeException(409, "Email đã tồn tại trong hệ thống");
            }
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (employeeRepository.existsByPhoneAndNotId(request.getPhoneNumber().trim(), employeeId.trim())) {
                throw new EmployeeException(409, "Số điện thoại đã tồn tại trong hệ thống");
            }
        }

        String position = request.getPosition();
        if (position != null && !position.trim().isEmpty()) {
            position = position.trim().toUpperCase();
            if (!"ADMIN".equals(position) && !"STAFF".equals(position)) {
                throw new EmployeeException(400, "Chức vụ không hợp lệ, phải là ADMIN hoặc STAFF");
            }
        }

        String hashedPassword = null;
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getNewPassword().length() < 6) {
                throw new EmployeeException(400, "Mật khẩu mới phải từ 6 ký tự trở lên");
            }
            hashedPassword = PasswordUtil.hash(request.getNewPassword());
        }

        employeeRepository.updateEmployee(
                employeeId.trim(),
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getBirthDate(),
                position,
                hashedPassword);

        Employee updated = employeeRepository.findById(employeeId.trim()).orElse(opt.get());
        EmployeeResponse.EmployeeData data = new EmployeeResponse.EmployeeData(
                updated.getMaNhanVien(),
                updated.getFullName(),
                updated.getUsername(),
                updated.getEmail(),
                updated.getPhoneNumber(),
                updated.getChucVu() != null ? updated.getChucVu().name() : "STAFF",
                updated.isActive());

        return new EmployeeResponse.ApiResponse<>(200, "Cập nhật tài khoản nhân viên thành công", data);
    }

    public EmployeeResponse.ApiResponse<EmployeeResponse.UpdateStatusData> updateStatus(
            String employeeId, EmployeeRequest.UpdateStatusRequest request, String currentAdminId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new EmployeeException(400, "Thiếu ID nhân viên");
        }
        if (request == null || request.getStatus() == null) {
            throw new EmployeeException(400, "Thiếu hoặc sai định dạng trạng thái (status phải là boolean)");
        }

        String targetId = employeeId.trim();

        if (currentAdminId != null && targetId.equals(currentAdminId.trim())
                && Boolean.FALSE.equals(request.getStatus())) {
            throw new EmployeeException(403, "Người gọi không phải là ADMIN, hoặc Admin tự khóa chính mình");
        }

        Optional<Employee> opt = employeeRepository.findById(targetId);
        if (opt.isEmpty()) {
            throw new EmployeeException(404, "Không tìm thấy nhân viên");
        }

        employeeRepository.updateStatus(targetId, request.getStatus());

        EmployeeResponse.UpdateStatusData data = new EmployeeResponse.UpdateStatusData(targetId, request.getStatus());
        return new EmployeeResponse.ApiResponse<>(200, "Cập nhật trạng thái nhân viên thành công", data);
    }
}
