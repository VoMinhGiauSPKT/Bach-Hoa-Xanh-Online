package com.mycompany.bachhoaxanhonline.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class AuthRequest {

    public static class RegisterRequest {

        @NotBlank(message = "Tên người dùng không được để trống")
        private String tenND;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không đúng định dạng")
        private String soDienThoai;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        private String password;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate ngaySinh;

        public RegisterRequest() {
        }

        public RegisterRequest(String tenND, String soDienThoai, String password, LocalDate ngaySinh) {
            this.tenND = tenND;
            this.soDienThoai = soDienThoai;
            this.password = password;
            this.ngaySinh = ngaySinh;
        }

        public String getTenND() {
            return tenND;
        }

        public void setTenND(String tenND) {
            this.tenND = tenND;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public LocalDate getNgaySinh() {
            return ngaySinh;
        }

        public void setNgaySinh(LocalDate ngaySinh) {
            this.ngaySinh = ngaySinh;
        }
    }

    public static class LoginRequest {

        @NotBlank(message = "Số điện thoại không được để trống")
        private String soDienThoai;

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password;

        public LoginRequest() {
        }

        public LoginRequest(String soDienThoai, String password) {
            this.soDienThoai = soDienThoai;
            this.password = password;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
