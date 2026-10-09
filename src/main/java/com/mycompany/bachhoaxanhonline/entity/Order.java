package com.mycompany.bachhoaxanhonline.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"DonHang\"")
@NamedQueries({
    @NamedQuery(name = "Order.findByCustomerId", query = "SELECT o FROM Order o WHERE o.maKhachHang = :maKhachHang ORDER BY o.ngayLap DESC"),
    @NamedQuery(name = "Order.findAll", query = "SELECT o FROM Order o ORDER BY o.ngayLap DESC")
})
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maDonHang\"", nullable = false, length = 255)
    private String maDonHang;

    @Column(name = "\"maKhachHang\"", nullable = false, length = 255)
    private String maKhachHang;

    @Column(name = "\"ngayLap\"", nullable = false)
    private LocalDateTime ngayLap;

    @Column(name = "\"tongTien\"", nullable = false)
    private BigDecimal tongTien;

    @Column(name = "\"trangThai\"", nullable = false, length = 255)
    private String trangThai; // DANGXULY, DATHANHTOAN, DAHUY

    @Column(name = "\"tenNguoiNhan\"", length = 255)
    private String tenNguoiNhan;

    @Column(name = "\"soDienThoaiNhan\"", length = 255)
    private String soDienThoaiNhan;

    @Column(name = "\"diaChiGiaoHang\"", length = 1000)
    private String diaChiGiaoHang;

    @Column(name = "\"Deleted\"")
    private Boolean deleted = false;

    @Column(name = "\"ngayHetHanThanhToan\"")
    private LocalDateTime ngayHetHanThanhToan;

    @Column(name = "\"tongTienSauGiamGia\"")
    private BigDecimal tongTienSauGiamGia;

    @Column(name = "\"tienGiamGia\"")
    private BigDecimal tienGiamGia;

    @Column(name = "\"ghiChu\"", length = 1000)
    private String ghiChu;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"maDonHang\"", referencedColumnName = "\"maDonHang\"", insertable = false, updatable = false)
    private List<LineItem> lineItems = new ArrayList<>();

    public Order() {
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

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

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getNgayHetHanThanhToan() {
        return ngayHetHanThanhToan;
    }

    public void setNgayHetHanThanhToan(LocalDateTime ngayHetHanThanhToan) {
        this.ngayHetHanThanhToan = ngayHetHanThanhToan;
    }

    public BigDecimal getTongTienSauGiamGia() {
        return tongTienSauGiamGia;
    }

    public void setTongTienSauGiamGia(BigDecimal tongTienSauGiamGia) {
        this.tongTienSauGiamGia = tongTienSauGiamGia;
    }

    public BigDecimal getTienGiamGia() {
        return tienGiamGia;
    }

    public void setTienGiamGia(BigDecimal tienGiamGia) {
        this.tienGiamGia = tienGiamGia;
    }
}
