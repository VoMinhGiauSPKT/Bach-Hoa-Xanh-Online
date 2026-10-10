package com.mycompany.bachhoaxanhonline.module.order;

import com.mycompany.bachhoaxanhonline.entity.*;
public class OrderRequest {

    public static class CreateOrderRequest {
        private String tenNguoiNhan;
        private String soDienThoaiNhan;
        private String diaChiGiaoHang;

        private String ghiChu;
        private String phuongThucTT;

        public String getTenNguoiNhan() {
            return tenNguoiNhan;
        }

        public void setTenNguoiNhan(String tenNguoiNhan) {
            this.tenNguoiNhan = tenNguoiNhan;
        }

        public String getSoDienThoaiNhan() {
            return soDienThoaiNhan;
        }

        public void setSoDienThoaiNhan(String soDienThoaiNhan) {
            this.soDienThoaiNhan = soDienThoaiNhan;
        }

        public String getDiaChiGiaoHang() {
            return diaChiGiaoHang;
        }

        public void setDiaChiGiaoHang(String diaChiGiaoHang) {
            this.diaChiGiaoHang = diaChiGiaoHang;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }

        public String getPhuongThucTT() {
            return phuongThucTT;
        }

        public void setPhuongThucTT(String phuongThucTT) {
            this.phuongThucTT = phuongThucTT;
        }
    }
}
