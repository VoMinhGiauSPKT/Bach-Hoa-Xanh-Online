package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class EmployeeRepository {

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NguoiDung\" WHERE \"tenDangNhap\" = :username")
                    .setParameter("username", username)
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
                    .setParameter("phone", phoneNumber)
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public void createEmployee(String employeeId, String fullName, String username, String phoneNumber, String hashedPassword, String position) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. ThÃªm báº£n ghi vÃ o báº£ng NguoiDung
            em.createNativeQuery(
                    "INSERT INTO \"NguoiDung\" (\"maNguoiDung\", \"tenND\", \"tenDangNhap\", \"soDienThoai\", \"matKhauHashed\", \"Deleted\") "
                    + "VALUES (:id, :fullName, :username, :phone, :hash, false)")
                    .setParameter("id", employeeId)
                    .setParameter("fullName", fullName)
                    .setParameter("username", username)
                    .setParameter("phone", phoneNumber)
                    .setParameter("hash", hashedPassword)
                    .executeUpdate();

            // 2. ThÃªm báº£n ghi vÃ o báº£ng NhanVien vá»›i enum_chucvu_nhanvien vÃ  CURRENT_DATE
            em.createNativeQuery(
                    "INSERT INTO \"NhanVien\" (\"maNhanVien\", \"ngayVaoLam\", \"chucVu\") "
                    + "VALUES (:id, CURRENT_DATE, CAST(:cv AS enum_chucvu_nhanvien))")
                    .setParameter("id", employeeId)
                    .setParameter("cv", position)
                    .executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lá»—i lÆ°u thÃ´ng tin nhÃ¢n viÃªn: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
