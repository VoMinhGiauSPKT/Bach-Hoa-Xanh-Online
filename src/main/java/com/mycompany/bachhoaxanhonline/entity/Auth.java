package com.mycompany.bachhoaxanhonline.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "\"NguoiDung\"")
@NamedQueries({
    @NamedQuery(name = "Auth.findByUsername", query = "SELECT a FROM Auth a WHERE a.tenDangNhap = :tenDangNhap"),
    @NamedQuery(name = "Auth.existsByUsername", query = "SELECT COUNT(a) FROM Auth a WHERE a.tenDangNhap = :tenDangNhap"),
    @NamedQuery(name = "Auth.existsByEmail", query = "SELECT COUNT(a) FROM Auth a WHERE a.email = :email"),
    @NamedQuery(name = "Auth.findByPhone", query = "SELECT a FROM Auth a WHERE a.soDienThoai = :soDienThoai"),
    @NamedQuery(name = "Auth.existsByPhone", query = "SELECT COUNT(a) FROM Auth a WHERE a.soDienThoai = :soDienThoai")
})
public class Auth implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maNguoiDung\"", nullable = false, length = 255)
    private String maNguoiDung;

    @Column(name = "\"tenND\"", nullable = false, length = 255)
    private String tenND;

    @Column(name = "\"tenDangNhap\"", length = 255)
    private String tenDangNhap;

    @Column(name = "\"email\"", length = 255)
    private String email;

    @Column(name = "\"soDienThoai\"", nullable = false, length = 255)
    private String soDienThoai;

    @Column(name = "\"matKhauHashed\"", nullable = false, length = 255)
    private String matKhauHashed;

    @Column(name = "\"ngaySinh\"")
    private LocalDate ngaySinh;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    @Column(name = "\"refreshToken\"")
    private String refreshToken;

    public Auth() {
    }

    public Auth(String maNguoiDung, String tenND, String tenDangNhap, String email, String soDienThoai, String matKhauHashed, LocalDate ngaySinh) {
        this.maNguoiDung = maNguoiDung;
        this.tenND = tenND;
        this.tenDangNhap = tenDangNhap;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.matKhauHashed = matKhauHashed;
        this.ngaySinh = ngaySinh;
        this.deleted = false;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTenND() {
        return tenND;
    }

    public void setTenND(String tenND) {
        this.tenND = tenND;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getMatKhauHashed() {
        return matKhauHashed;
    }

    public void setMatKhauHashed(String matKhauHashed) {
        this.matKhauHashed = matKhauHashed;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
