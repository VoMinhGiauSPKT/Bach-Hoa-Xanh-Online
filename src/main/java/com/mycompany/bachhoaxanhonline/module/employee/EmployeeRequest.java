package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class EmployeeRequest {

    public static class CreateEmployeeRequest {

        @NotBlank(message = "Họ và tên không được để trống")
        private String fullName;

        @NotBlank(message = "Tên đăng nhập không được để trống")
        private String username;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        private String password;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không đúng định dạng")
        private String phoneNumber;

        private String position = "STAFF";

        public CreateEmployeeRequest() {
        }

        public CreateEmployeeRequest(String fullName, String username, String password, String phoneNumber,
                String position) {
            this.fullName = fullName;
            this.username = username;
            this.password = password;
            this.phoneNumber = phoneNumber;
            this.position = position != null ? position : "STAFF";
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static class UpdateEmployeeRequest {

        private String fullName;

        @Email(message = "Email không đúng định dạng")
        private String email;

        @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không đúng định dạng")
        private String phoneNumber;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        private String position;

        @Size(min = 6, message = "Mật khẩu mới phải từ 6 ký tự trở lên")
        private String newPassword;

        public UpdateEmployeeRequest() {
        }

        public UpdateEmployeeRequest(String fullName, String email, String phoneNumber, LocalDate birthDate,
                String position, String newPassword) {
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.birthDate = birthDate;
            this.position = position;
            this.newPassword = newPassword;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class UpdateStatusRequest {

        @NotNull(message = "Trạng thái không được để trống")
        private Boolean status;

        public UpdateStatusRequest() {
        }

        public UpdateStatusRequest(Boolean status) {
            this.status = status;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }
    }

    public static class EmployeeFilterRequest {
        private int page = 1;
        private int limit = 10;
        private String keyword;
        private String position;
        private Boolean status;

        public EmployeeFilterRequest() {
        }

        public EmployeeFilterRequest(int page, int limit, String keyword, String position, Boolean status) {
            this.page = page > 0 ? page : 1;
            this.limit = limit > 0 ? limit : 10;
            this.keyword = keyword;
            this.position = position;
            this.status = status;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }
    }
}
