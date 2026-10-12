package com.mycompany.bachhoaxanhonline.module.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierRequest {

    public static class CreateSupplierRequest {
        private String supplierId;

        @NotBlank(message = "Tên nhà cung cấp không được để trống")
        @Size(max = 200, message = "Tên nhà cung cấp tối đa 200 ký tự")
        private String supplierName;

        private String email;
        private String phoneNumber;
        private Boolean active = true;

        public CreateSupplierRequest() {
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
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

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }

    public static class UpdateSupplierRequest {
        @NotBlank(message = "Tên nhà cung cấp không được để trống")
        @Size(max = 200, message = "Tên nhà cung cấp tối đa 200 ký tự")
        private String supplierName;

        private String email;
        private String phoneNumber;
        private Boolean active = true;

        public UpdateSupplierRequest() {
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
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

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }
}
