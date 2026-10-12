package com.mycompany.bachhoaxanhonline.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "\"NhaCungCap\"")
@NamedQueries({
    @NamedQuery(name = "Supplier.findAllActive", query = "SELECT s FROM Supplier s WHERE s.deleted = false ORDER BY s.maNhaCungCap ASC"),
    @NamedQuery(name = "Supplier.findAll", query = "SELECT s FROM Supplier s ORDER BY s.maNhaCungCap ASC"),
    @NamedQuery(name = "Supplier.existsByName", query = "SELECT COUNT(s) FROM Supplier s WHERE LOWER(s.tenNhaCungCap) = LOWER(:tenNhaCungCap) AND s.deleted = false"),
    @NamedQuery(name = "Supplier.existsByNameAndNotId", query = "SELECT COUNT(s) FROM Supplier s WHERE LOWER(s.tenNhaCungCap) = LOWER(:tenNhaCungCap) AND s.maNhaCungCap <> :maNhaCungCap AND s.deleted = false")
})
public class Supplier implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maNhaCungCap\"", nullable = false, length = 50)
    private String maNhaCungCap;

    @Column(name = "\"tenNhaCungCap\"", nullable = false, length = 200)
    private String tenNhaCungCap;

    @Column(name = "\"email\"", length = 150)
    private String email;

    @Column(name = "\"soDienThoai\"", length = 20)
    private String soDienThoai;

    @Column(name = "\"dangHopTac\"", nullable = false)
    private Boolean dangHopTac = true;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    public Supplier() {
    }

    public Supplier(String maNhaCungCap, String tenNhaCungCap, String email, String soDienThoai, Boolean dangHopTac) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.dangHopTac = dangHopTac != null ? dangHopTac : true;
        this.deleted = false;
    }

    public Supplier(String maNhaCungCap, String tenNhaCungCap, String email, String soDienThoai, Boolean dangHopTac, Boolean deleted) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.dangHopTac = dangHopTac != null ? dangHopTac : true;
        this.deleted = deleted != null ? deleted : false;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
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

    public Boolean getDangHopTac() {
        return dangHopTac;
    }

    public void setDangHopTac(Boolean dangHopTac) {
        this.dangHopTac = dangHopTac;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
