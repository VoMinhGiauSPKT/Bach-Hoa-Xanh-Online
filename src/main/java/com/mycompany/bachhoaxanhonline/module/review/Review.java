package com.mycompany.bachhoaxanhonline.module.review;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"DanhGia\"")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"maDanhGia\"", nullable = false)
    private Long maDanhGia;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    @Column(name = "\"ngayDang\"", nullable = false)
    private LocalDateTime ngayDang;

    @Column(name = "\"noiDung\"")
    private String noiDung;

    @Column(name = "\"soSao\"", nullable = false)
    private Integer soSao;

    @Column(name = "\"maKhachHang\"", nullable = false, length = 255)
    private String maKhachHang;

    @Column(name = "\"maSanPham\"", nullable = false, length = 255)
    private String maSanPham;

    public Review() {
    }

    public Review(String maKhachHang, String maSanPham, Integer soSao, String noiDung, LocalDateTime ngayDang) {
        this.maKhachHang = maKhachHang;
        this.maSanPham = maSanPham;
        this.soSao = soSao;
        this.noiDung = noiDung;
        this.ngayDang = ngayDang;
        this.deleted = false;
    }

    public Long getMaDanhGia() {
        return maDanhGia;
    }

    public void setMaDanhGia(Long maDanhGia) {
        this.maDanhGia = maDanhGia;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getNgayDang() {
        return ngayDang;
    }

    public void setNgayDang(LocalDateTime ngayDang) {
        this.ngayDang = ngayDang;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public Integer getSoSao() {
        return soSao;
    }

    public void setSoSao(Integer soSao) {
        this.soSao = soSao;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }
}
