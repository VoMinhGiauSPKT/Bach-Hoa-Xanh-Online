package com.mycompany.bachhoaxanhonline.module.order;

import com.mycompany.bachhoaxanhonline.module.cart.CartResponse.LineItemData;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    public static class ApiResponse<T> {
        private int status;
        private String message;
        private T data;

        public ApiResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public ApiResponse(int status, String message, T data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }

    public static class OrderListData {
        private List<OrderSummary> items;
        private int totalPages;
        private int currentPage;

        public List<OrderSummary> getItems() { return items; }
        public void setItems(List<OrderSummary> items) { this.items = items; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    }

    public static class OrderSummary {
        private String maDonHang;
        private LocalDateTime ngayLap;
        private BigDecimal tongTien;
        private String trangThai;
        private int soLuongMatHang;

        public String getMaDonHang() { return maDonHang; }
        public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
        public LocalDateTime getNgayLap() { return ngayLap; }
        public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
        public BigDecimal getTongTien() { return tongTien; }
        public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public int getSoLuongMatHang() { return soLuongMatHang; }
        public void setSoLuongMatHang(int soLuongMatHang) { this.soLuongMatHang = soLuongMatHang; }
    }

    public static class OrderDetailData {
        private String maDonHang;
        private String maKhachHang;
        private LocalDateTime ngayLap;
        private BigDecimal tongTien;
        private String trangThai;
        private String phuongThucTT;
        private String tenNguoiNhan;
        private String soDienThoaiNhan;
        private String diaChiGiaoHang;
        private List<LineItemData> items;

        public String getMaDonHang() { return maDonHang; }
        public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
        public String getMaKhachHang() { return maKhachHang; }
        public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
        public LocalDateTime getNgayLap() { return ngayLap; }
        public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
        public BigDecimal getTongTien() { return tongTien; }
        public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public String getPhuongThucTT() { return phuongThucTT; }
        public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }
        public String getTenNguoiNhan() { return tenNguoiNhan; }
        public void setTenNguoiNhan(String tenNguoiNhan) { this.tenNguoiNhan = tenNguoiNhan; }
        public String getSoDienThoaiNhan() { return soDienThoaiNhan; }
        public void setSoDienThoaiNhan(String soDienThoaiNhan) { this.soDienThoaiNhan = soDienThoaiNhan; }
        public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
        public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }
        public List<LineItemData> getItems() { return items; }
        public void setItems(List<LineItemData> items) { this.items = items; }
    }
}
