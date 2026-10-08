package com.mycompany.bachhoaxanhonline.module.promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"KhuyenMai\"")
public class Promotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maKhuyenMai\"", nullable = false, length = 50)
    private String maKhuyenMai;

    @Column(name = "\"tenKhuyenMai\"", nullable = false, length = 255)
    private String tenKhuyenMai;

    @Column(name = "\"moTa\"")
    private String moTa;

    @Column(name = "\"loaiKhuyenMai\"", nullable = false)
    private String loaiKhuyenMai;

    @Column(name = "\"giaTriGiam\"", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "\"giamToiDa\"", precision = 12, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "\"donHangToiThieu\"", nullable = false, precision = 12, scale = 2)
    private BigDecimal donHangToiThieu;

    @Column(name = "\"soLuongDung\"", nullable = false)
    private Integer soLuongDung = 100;

    @Column(name = "\"ngayBatDau\"", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "\"ngayKetThuc\"", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    public Promotion() {
    }

    public Promotion(String maKhuyenMai, String tenKhuyenMai, String moTa, String loaiKhuyenMai,
                     BigDecimal giaTriGiam, BigDecimal giamToiDa, BigDecimal donHangToiThieu,
                     Integer soLuongDung, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.moTa = moTa;
        this.loaiKhuyenMai = loaiKhuyenMai;
        this.giaTriGiam = giaTriGiam;
        this.giamToiDa = giamToiDa;
        this.donHangToiThieu = donHangToiThieu;
        this.soLuongDung = soLuongDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.deleted = false;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getLoaiKhuyenMai() {
        return loaiKhuyenMai;
    }

    public void setLoaiKhuyenMai(String loaiKhuyenMai) {
        this.loaiKhuyenMai = loaiKhuyenMai;
    }

    public BigDecimal getGiaTriGiam() {
        return giaTriGiam;
    }

    public void setGiaTriGiam(BigDecimal giaTriGiam) {
        this.giaTriGiam = giaTriGiam;
    }

    public BigDecimal getGiamToiDa() {
        return giamToiDa;
    }

    public void setGiamToiDa(BigDecimal giamToiDa) {
        this.giamToiDa = giamToiDa;
    }

    public BigDecimal getDonHangToiThieu() {
        return donHangToiThieu;
    }

    public void setDonHangToiThieu(BigDecimal donHangToiThieu) {
        this.donHangToiThieu = donHangToiThieu;
    }

    public Integer getSoLuongDung() {
        return soLuongDung;
    }

    public void setSoLuongDung(Integer soLuongDung) {
        this.soLuongDung = soLuongDung;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
