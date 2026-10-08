package com.mycompany.bachhoaxanhonline.module.order;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"ThanhToan\"")
@NamedQueries({
    @NamedQuery(name = "Payment.findByOrderId", query = "SELECT p FROM Payment p WHERE p.order.maDonHang = :maDonHang ORDER BY p.ngayThanhToan DESC")
})
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maTT\"", nullable = false, length = 255)
    private String maTT;

    @Column(name = "\"soTien\"", nullable = false)
    private BigDecimal soTien;

    @Column(name = "\"trangThai\"", nullable = false, length = 255)
    private String trangThai; // THANHCONG, THATBAI

    @Column(name = "\"ngayThanhToan\"", nullable = false)
    private LocalDateTime ngayThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"maDonHang\"", referencedColumnName = "\"maDonHang\"")
    private Order order;

    public Payment() {
    }

    public String getMaTT() {
        return maTT;
    }

    public void setMaTT(String maTT) {
        this.maTT = maTT;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(LocalDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
