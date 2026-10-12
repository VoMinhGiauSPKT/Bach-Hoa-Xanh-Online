package com.mycompany.bachhoaxanhonline.module.supplier;

import com.fasterxml.jackson.annotation.JsonInclude;

public class SupplierResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SupplierItem {
        private String supplierId;
        private String supplierName;
        private String email;
        private String phoneNumber;
        private Boolean active;
        private Boolean deleted;

        public SupplierItem() {
        }

        public SupplierItem(String supplierId, String supplierName, String email, String phoneNumber, Boolean active, Boolean deleted) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.active = active;
            this.deleted = deleted;
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

        public String getSoDienThoai() {
            return phoneNumber;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.phoneNumber = soDienThoai;
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

        public Boolean getDangHopTac() {
            return active;
        }

        public void setDangHopTac(Boolean dangHopTac) {
            this.active = dangHopTac;
        }

        public Boolean getDeleted() {
            return deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }
    }
}
