package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.module.auth.Auth;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "\"NhanVien\"")
@NamedQueries({
        @NamedQuery(name = "Employee.findAll", query = "SELECT e FROM Employee e WHERE e.deleted = false"),
        @NamedQuery(name = "Employee.findById", query = "SELECT e FROM Employee e WHERE e.maNhanVien = :maNhanVien AND e.deleted = false"),
        @NamedQuery(name = "Employee.findByRole", query = "SELECT e FROM Employee e WHERE e.chucVu = :chucVu AND e.deleted = false"),
        @NamedQuery(name = "Employee.findByUsername", query = "SELECT e FROM Employee e JOIN e.user u WHERE u.tenDangNhap = :tenDangNhap AND e.deleted = false"),
        @NamedQuery(name = "Employee.findByPhone", query = "SELECT e FROM Employee e JOIN e.user u WHERE u.soDienThoai = :soDienThoai AND e.deleted = false")
})
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "\"maNhanVien\"", nullable = false, length = 50)
    private String maNhanVien;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @MapsId
    @JoinColumn(name = "\"maNhanVien\"", referencedColumnName = "\"maNguoiDung\"")
    private Auth user;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"chucVu\"", columnDefinition = "enum_chucvu_nhanvien", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private EmployeeRole chucVu = EmployeeRole.STAFF;

    @Column(name = "\"ngayVaoLam\"", nullable = false)
    private LocalDate ngayVaoLam = LocalDate.now();

    @Column(name = "\"Deleted\"", nullable = false)
    private Boolean deleted = false;

    public Employee() {
        this.chucVu = EmployeeRole.STAFF;
        this.ngayVaoLam = LocalDate.now();
        this.deleted = false;
    }

    public Employee(Auth user, EmployeeRole chucVu) {
        this.user = user;
        if (user != null) {
            this.maNhanVien = user.getMaNguoiDung();
        }
        this.chucVu = chucVu != null ? chucVu : EmployeeRole.STAFF;
        this.ngayVaoLam = LocalDate.now();
        this.deleted = false;
    }

    public Employee(String maNhanVien, Auth user, EmployeeRole chucVu, LocalDate ngayVaoLam, Boolean deleted) {
        this.maNhanVien = maNhanVien;
        this.user = user;
        if (this.maNhanVien == null && user != null) {
            this.maNhanVien = user.getMaNguoiDung();
        }
        this.chucVu = chucVu != null ? chucVu : EmployeeRole.STAFF;
        this.ngayVaoLam = ngayVaoLam != null ? ngayVaoLam : LocalDate.now();
        this.deleted = deleted != null ? deleted : false;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
        if (this.user != null && this.user.getMaNguoiDung() == null) {
            this.user.setMaNguoiDung(maNhanVien);
        }
    }

    public Auth getUser() {
        return user;
    }

    public void setUser(Auth user) {
        this.user = user;
        if (user != null && user.getMaNguoiDung() != null) {
            this.maNhanVien = user.getMaNguoiDung();
        }
    }

    public EmployeeRole getChucVu() {
        return chucVu;
    }

    public void setChucVu(EmployeeRole chucVu) {
        this.chucVu = chucVu != null ? chucVu : EmployeeRole.STAFF;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
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

    public boolean isActive() {
        return !Boolean.TRUE.equals(deleted) && (user == null || !Boolean.TRUE.equals(user.getDeleted()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Employee employee = (Employee) o;
        return Objects.equals(maNhanVien, employee.maNhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhanVien);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", chucVu=" + chucVu +
                ", ngayVaoLam=" + ngayVaoLam +
                ", deleted=" + deleted +
                '}';
    }
}
