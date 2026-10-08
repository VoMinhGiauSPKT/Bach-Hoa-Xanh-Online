package com.mycompany.bachhoaxanhonline.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "\"LoaiSanPham\"")
@NamedQueries({
    @NamedQuery(name = "Category.findAllActive", query = "SELECT c FROM Category c WHERE c.deleted = false ORDER BY c.maLoaiSanPham ASC"),
    @NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c ORDER BY c.maLoaiSanPham ASC"),
    @NamedQuery(name = "Category.existsByName", query = "SELECT COUNT(c) FROM Category c WHERE LOWER(c.tenLoaiSanPham) = LOWER(:tenLoai) AND c.deleted = false"),
    @NamedQuery(name = "Category.existsByNameAndNotId", query = "SELECT COUNT(c) FROM Category c WHERE LOWER(c.tenLoaiSanPham) = LOWER(:tenLoai) AND c.maLoaiSanPham <> :maLoai AND c.deleted = false")
})
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maLoaiSanPham\"", nullable = false, length = 255)
    private String maLoaiSanPham;

    @Column(name = "\"tenLoaiSanPham\"", nullable = false, length = 255)
    private String tenLoaiSanPham;

    @Column(name = "\"phanTramLoiNhuan\"", nullable = false)
    private Double phanTramLoiNhuan;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    public Category() {
    }

    public Category(String maLoaiSanPham, String tenLoaiSanPham, Double phanTramLoiNhuan) {
        this.maLoaiSanPham = maLoaiSanPham;
        this.tenLoaiSanPham = tenLoaiSanPham;
        this.phanTramLoiNhuan = phanTramLoiNhuan;
        this.deleted = false;
    }

    public Category(String maLoaiSanPham, String tenLoaiSanPham, Double phanTramLoiNhuan, Boolean deleted) {
        this.maLoaiSanPham = maLoaiSanPham;
        this.tenLoaiSanPham = tenLoaiSanPham;
        this.phanTramLoiNhuan = phanTramLoiNhuan;
        this.deleted = deleted != null ? deleted : false;
    }

    public String getMaLoaiSanPham() {
        return maLoaiSanPham;
    }

    public void setMaLoaiSanPham(String maLoaiSanPham) {
        this.maLoaiSanPham = maLoaiSanPham;
    }

    public String getTenLoaiSanPham() {
        return tenLoaiSanPham;
    }

    public void setTenLoaiSanPham(String tenLoaiSanPham) {
        this.tenLoaiSanPham = tenLoaiSanPham;
    }

    public Double getPhanTramLoiNhuan() {
        return phanTramLoiNhuan;
    }

    public void setPhanTramLoiNhuan(Double phanTramLoiNhuan) {
        this.phanTramLoiNhuan = phanTramLoiNhuan;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
