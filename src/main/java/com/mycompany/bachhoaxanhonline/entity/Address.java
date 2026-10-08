package com.mycompany.bachhoaxanhonline.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "\"DiaChi\"")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"maDiaChi\"", nullable = false)
    private Long maDiaChi;

    @Column(name = "\"tenNguoiNhan\"", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Column(name = "\"soDienThoai\"", nullable = false, length = 20)
    private String soDienThoai;

    @Column(name = "\"soNha\"", nullable = false, length = 255)
    private String soNha;

    @Column(name = "\"phuong\"", length = 100)
    private String phuong;

    @Column(name = "\"tinh\"", nullable = false, length = 100)
    private String tinh;

    @Column(name = "\"laMacDinh\"", nullable = false)
    private Boolean laMacDinh = false;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    @Column(name = "\"maKhachHang\"", nullable = false, length = 50)
    private String maKhachHang;

    public Address() {
    }

    public Address(Long maDiaChi, String tenNguoiNhan, String soDienThoai, String soNha,
                   String phuong, String tinh, Boolean laMacDinh, Boolean deleted, String maKhachHang) {
        this.maDiaChi = maDiaChi;
        this.tenNguoiNhan = tenNguoiNhan;
        this.soDienThoai = soDienThoai;
        this.soNha = soNha;
        this.phuong = phuong;
        this.tinh = tinh;
        this.laMacDinh = laMacDinh != null ? laMacDinh : false;
        this.deleted = deleted != null ? deleted : false;
        this.maKhachHang = maKhachHang;
    }

    public Long getMaDiaChi() {
        return maDiaChi;
    }

    public void setMaDiaChi(Long maDiaChi) {
        this.maDiaChi = maDiaChi;
    }

    public String getTenNguoiNhan() {
        return tenNguoiNhan;
    }

    public void setTenNguoiNhan(String tenNguoiNhan) {
        this.tenNguoiNhan = tenNguoiNhan;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getSoNha() {
        return soNha;
    }

    public void setSoNha(String soNha) {
        this.soNha = soNha;
    }

    public String getPhuong() {
        return phuong;
    }

    public void setPhuong(String phuong) {
        this.phuong = phuong;
    }

    public String getTinh() {
        return tinh;
    }

    public void setTinh(String tinh) {
        this.tinh = tinh;
    }

    public Boolean getLaMacDinh() {
        return laMacDinh;
    }

    public void setLaMacDinh(Boolean laMacDinh) {
        this.laMacDinh = laMacDinh;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }
}
