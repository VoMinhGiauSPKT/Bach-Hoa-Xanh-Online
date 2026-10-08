package com.mycompany.bachhoaxanhonline.module.user;

import com.mycompany.bachhoaxanhonline.module.auth.Auth;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "\"KhachHang\"")
@NamedQueries({
        @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c WHERE c.deleted = false"),
        @NamedQuery(name = "Customer.findById", query = "SELECT c FROM Customer c WHERE c.maKhachHang = :maKhachHang AND c.deleted = false"),
        @NamedQuery(name = "Customer.findByUsername", query = "SELECT c FROM Customer c JOIN c.user u WHERE u.tenDangNhap = :tenDangNhap AND c.deleted = false"),
        @NamedQuery(name = "Customer.findByPhone", query = "SELECT c FROM Customer c JOIN c.user u WHERE u.soDienThoai = :soDienThoai AND c.deleted = false")
})
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"maKhachHang\"", nullable = false, length = 50)
    private String maKhachHang;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @MapsId
    @JoinColumn(name = "\"maKhachHang\"", referencedColumnName = "\"maNguoiDung\"")
    private Auth user;

    @Column(name = "\"refreshToken\"")
    private String refreshToken;

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    public Customer() {
        this.deleted = false;
    }

    public Customer(Auth user) {
        this.user = user;
        if (user != null) {
            this.maKhachHang = user.getMaNguoiDung();
        }
        this.deleted = false;
    }

    public Customer(String maKhachHang, Auth user, String refreshToken, Boolean deleted) {
        this.maKhachHang = maKhachHang;
        this.user = user;
        if (this.maKhachHang == null && user != null) {
            this.maKhachHang = user.getMaNguoiDung();
        }
        this.refreshToken = refreshToken;
        this.deleted = deleted != null ? deleted : false;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
        if (this.user != null && this.user.getMaNguoiDung() == null) {
            this.user.setMaNguoiDung(maKhachHang);
        }
    }

    public Auth getUser() {
        return user;
    }

    public void setUser(Auth user) {
        this.user = user;
        if (user != null && user.getMaNguoiDung() != null) {
            this.maKhachHang = user.getMaNguoiDung();
        }
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getFullName() {
        return user != null ? user.getTenND() : null;
    }

    public String getUsername() {
        return user != null ? user.getTenDangNhap() : null;
    }

    public String getPhoneNumber() {
        return user != null ? user.getSoDienThoai() : null;
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    public LocalDate getBirthDate() {
        return user != null ? user.getNgaySinh() : null;
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(deleted) && (user == null || !Boolean.TRUE.equals(user.getDeleted()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Customer customer = (Customer) o;
        return Objects.equals(maKhachHang, customer.maKhachHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKhachHang);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
