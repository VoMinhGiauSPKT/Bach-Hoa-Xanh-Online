package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.module.category.Category;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "\"SanPham\"")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maSanPham\"", nullable = false, length = 255)
    private String maSanPham;

    @Column(name = "\"tenSanPham\"", nullable = false, length = 255)
    private String tenSanPham;

    @Column(name = "\"donViTinh\"", nullable = false, length = 255)
    private String donViTinh;

    @Column(name = "\"giaBan\"", nullable = false)
    private BigDecimal giaBan;

    @Column(name = "\"giaNhap\"", nullable = false)
    private BigDecimal giaNhap;

    @Column(name = "\"hanSuDung\"")
    private LocalDate hanSuDung;

    @Column(name = "\"hinhAnh\"")
    private String hinhAnh;

    @Column(name = "\"phiVAT\"", nullable = false)
    private Double phiVAT;

    @Column(name = "\"soLuong\"", nullable = false)
    private Integer soLuong;

    @Column(name = "\"maLoai\"", nullable = false, length = 255)
    private String maLoai;

    @Column(name = "\"maNhaCungCap\"", nullable = false, length = 255)
    private String maNhaCungCap;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"maLoai\"", referencedColumnName = "\"maLoaiSanPham\"", insertable = false, updatable = false)
    private Category category;

    public Product() {
    }

    public Product(String maSanPham, String tenSanPham, String donViTinh, BigDecimal giaBan, BigDecimal giaNhap,
                   LocalDate hanSuDung, String hinhAnh, Double phiVAT, Integer soLuong, String maLoai,
                   String maNhaCungCap, Boolean deleted) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
        this.giaNhap = giaNhap;
        this.hanSuDung = hanSuDung;
        this.hinhAnh = hinhAnh;
        this.phiVAT = phiVAT;
        this.soLuong = soLuong;
        this.maLoai = maLoai;
        this.maNhaCungCap = maNhaCungCap;
        this.deleted = deleted != null ? deleted : false;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public BigDecimal getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public Double getPhiVAT() {
        return phiVAT;
    }

    public void setPhiVAT(Double phiVAT) {
        this.phiVAT = phiVAT;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
