package com.mycompany.bachhoaxanhonline.module.cart;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"GioHang\"")
@NamedQueries({
    @NamedQuery(name = "Cart.findByCustomerId", query = "SELECT c FROM Cart c LEFT JOIN FETCH c.lineItems li LEFT JOIN FETCH li.product WHERE c.maKhachHang = :maKhachHang")
})
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maGioHang\"", nullable = false, length = 255)
    private String maGioHang;

    @Column(name = "\"maKhachHang\"", nullable = false, length = 255, unique = true)
    private String maKhachHang;

    @Column(name = "\"tongTien\"", nullable = false)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Column(name = "\"Deleted\"")
    private Boolean deleted = false;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineItem> lineItems = new ArrayList<>();

    public Cart() {
    }

    public Cart(String maGioHang, String maKhachHang, BigDecimal tongTien) {
        this.maGioHang = maGioHang;
        this.maKhachHang = maKhachHang;
        this.tongTien = tongTien != null ? tongTien : BigDecimal.ZERO;
    }

    public String getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(String maGioHang) {
        this.maGioHang = maGioHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }
}
