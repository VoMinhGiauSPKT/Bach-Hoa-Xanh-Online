package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository {

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NguoiDung\" WHERE \"tenDangNhap\" = :username")
                    .setParameter("username", username.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NguoiDung\" WHERE \"soDienThoai\" = :phone")
                    .setParameter("phone", phoneNumber.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NguoiDung\" WHERE \"email\" = :email")
                    .setParameter("email", email.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByPhoneAndNotId(String phoneNumber, String excludeEmployeeId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NguoiDung\" WHERE \"soDienThoai\" = :phone AND \"maNguoiDung\" <> :id")
                    .setParameter("phone", phoneNumber.trim())
                    .setParameter("id", excludeEmployeeId)
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByEmailAndNotId(String email, String excludeEmployeeId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NguoiDung\" WHERE \"email\" = :email AND \"maNguoiDung\" <> :id")
                    .setParameter("email", email.trim())
                    .setParameter("id", excludeEmployeeId)
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public void createEmployee(String employeeId, String fullName, String username, String phoneNumber,
            String hashedPassword, String position) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            em.createNativeQuery(
                    "INSERT INTO \"NguoiDung\" (\"maNguoiDung\", \"tenND\", \"tenDangNhap\", \"soDienThoai\", \"matKhauHashed\", \"Deleted\") "
                            + "VALUES (:id, :fullName, :username, :phone, :hash, false)")
                    .setParameter("id", employeeId)
                    .setParameter("fullName", fullName)
                    .setParameter("username", username)
                    .setParameter("phone", phoneNumber)
                    .setParameter("hash", hashedPassword)
                    .executeUpdate();

            em.createNativeQuery(
                    "INSERT INTO \"NhanVien\" (\"maNhanVien\", \"ngayVaoLam\", \"chucVu\", \"Deleted\") "
                            + "VALUES (:id, CURRENT_DATE, CAST(:cv AS enum_chucvu_nhanvien), false)")
                    .setParameter("id", employeeId)
                    .setParameter("cv", position)
                    .executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi lưu thông tin nhân viên: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findById(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Employee> query = em.createQuery(
                    "SELECT e FROM Employee e JOIN FETCH e.user WHERE e.maNhanVien = :id", Employee.class);
            query.setParameter("id", employeeId.trim());
            List<Employee> list = query.getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public List<Employee> findEmployees(int page, int limit, String keyword, String position, Boolean status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT e FROM Employee e JOIN FETCH e.user u WHERE 1=1");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(
                        " AND (LOWER(u.tenND) LIKE :kw OR LOWER(u.tenDangNhap) LIKE :kw OR u.soDienThoai LIKE :kw)");
            }
            if (position != null && !position.trim().isEmpty()) {
                jpql.append(" AND e.chucVu = :pos");
            }
            if (status != null) {
                if (status) {
                    jpql.append(" AND e.deleted = false AND u.deleted = false");
                } else {
                    jpql.append(" AND (e.deleted = true OR u.deleted = true)");
                }
            }
            jpql.append(" ORDER BY e.ngayVaoLam DESC, e.maNhanVien DESC");

            TypedQuery<Employee> query = em.createQuery(jpql.toString(), Employee.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (position != null && !position.trim().isEmpty()) {
                query.setParameter("pos", EmployeeRole.fromString(position));
            }

            query.setFirstResult((page - 1) * limit);
            query.setMaxResults(limit);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countEmployees(String keyword, String position, Boolean status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM Employee e JOIN e.user u WHERE 1=1");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(
                        " AND (LOWER(u.tenND) LIKE :kw OR LOWER(u.tenDangNhap) LIKE :kw OR u.soDienThoai LIKE :kw)");
            }
            if (position != null && !position.trim().isEmpty()) {
                jpql.append(" AND e.chucVu = :pos");
            }
            if (status != null) {
                if (status) {
                    jpql.append(" AND e.deleted = false AND u.deleted = false");
                } else {
                    jpql.append(" AND (e.deleted = true OR u.deleted = true)");
                }
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (position != null && !position.trim().isEmpty()) {
                query.setParameter("pos", EmployeeRole.fromString(position));
            }

            Long count = query.getSingleResult();
            return count != null ? count : 0L;
        } finally {
            em.close();
        }
    }

    public void updateEmployee(String employeeId, String fullName, String email, String phoneNumber,
            LocalDate birthDate, String position, String hashedPassword) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, employeeId);
            if (emp != null) {
                Auth user = emp.getUser();
                if (user != null) {
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        user.setTenND(fullName.trim());
                    }
                    if (email != null) {
                        user.setEmail(email.trim().isEmpty() ? null : email.trim());
                    }
                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        user.setSoDienThoai(phoneNumber.trim());
                    }
                    if (birthDate != null) {
                        user.setNgaySinh(birthDate);
                    }
                    if (hashedPassword != null && !hashedPassword.trim().isEmpty()) {
                        user.setMatKhauHashed(hashedPassword);
                        user.setRefreshToken(null); // Thu hồi JWT Refresh Token phiên làm việc cũ
                    }
                }
                if (position != null && !position.trim().isEmpty()) {
                    emp.setChucVu(EmployeeRole.fromString(position));
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật nhân viên: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void updateStatus(String employeeId, boolean status) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, employeeId);
            if (emp != null) {
                boolean isDeleted = !status;
                emp.setDeleted(isDeleted);
                if (emp.getUser() != null) {
                    emp.getUser().setDeleted(isDeleted);
                    if (isDeleted) {
                        emp.getUser().setRefreshToken(null); // Văng phiên làm việc ngay khi bị khóa
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật trạng thái nhân viên: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
