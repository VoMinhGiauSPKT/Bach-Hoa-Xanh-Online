package com.mycompany.bachhoaxanhonline.module.address;

import java.io.Serializable;

public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long maDiaChi;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String soNha;
    private String phuong;
    private String tinh;
    private Boolean laMacDinh = false;
    private Boolean deleted = false;
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
