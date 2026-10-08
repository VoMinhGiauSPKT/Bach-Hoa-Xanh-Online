package com.mycompany.bachhoaxanhonline.module.cart;

import com.mycompany.bachhoaxanhonline.module.product.Product;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "\"LineItem\"")
@NamedQueries({
    @NamedQuery(name = "LineItem.findByCartId", query = "SELECT l FROM LineItem l WHERE l.cart.maGioHang = :maGioHang"),
    @NamedQuery(name = "LineItem.findByOrderId", query = "SELECT l FROM LineItem l WHERE l.maDonHang = :maDonHang")
})
public class LineItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"lineItemID\"", nullable = false)
    private Long lineItemId;

    @Column(name = "\"maSanPham\"", nullable = false, length = 255)
    private String maSanPham;

    @Column(name = "\"maDonHang\"", length = 255)
    private String maDonHang;

    @Column(name = "\"soLuong\"", nullable = false)
    private Integer soLuong;

    @Column(name = "\"thanhTien\"", nullable = false)
    private BigDecimal thanhTien;

    @Column(name = "\"Deleted\"")
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"maGioHang\"", referencedColumnName = "\"maGioHang\"")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"maSanPham\"", referencedColumnName = "\"maSanPham\"", insertable = false, updatable = false)
    private Product product;

    public LineItem() {
    }

    public LineItem(String maSanPham, Integer soLuong, BigDecimal thanhTien, Cart cart, String maDonHang) {
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien;
        this.cart = cart;
        this.maDonHang = maDonHang;
    }

    public Long getLineItemId() {
        return lineItemId;
    }

    public void setLineItemId(Long lineItemId) {
        this.lineItemId = lineItemId;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
