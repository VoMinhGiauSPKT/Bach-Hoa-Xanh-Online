package com.mycompany.bachhoaxanhonline.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "\"NguoiDung\"")
@NamedQueries({
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

    public Auth(String maNguoiDung, String tenND, String soDienThoai, String matKhauHashed, LocalDate ngaySinh) {
        this.maNguoiDung = maNguoiDung;
        this.tenND = tenND;
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
