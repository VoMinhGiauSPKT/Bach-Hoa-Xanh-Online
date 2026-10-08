package com.mycompany.bachhoaxanhonline.module.auth;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.Optional;
import java.util.UUID;

public class AuthRepository {

    public Optional<Auth> findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Auth auth = em.createNamedQuery("Auth.findByUsername", Auth.class)
                    .setParameter("tenDangNhap", username)
                    .getSingleResult();
            return Optional.ofNullable(auth);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createNamedQuery("Auth.existsByUsername", Long.class)
                    .setParameter("tenDangNhap", username)
                    .getSingleResult();
            return count != null && count > 0;
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
            Long count = em.createNamedQuery("Auth.existsByEmail", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByPhone(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createNamedQuery("Auth.existsByPhone", Long.class)
                    .setParameter("soDienThoai", soDienThoai)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public Optional<Auth> findById(String id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Auth auth = em.find(Auth.class, id);
            return Optional.ofNullable(auth);
        } finally {
            em.close();
        }
    }

    public String getEmployeePosition(String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Object result = em.createNativeQuery(
                    "SELECT CAST(\"chucVu\" AS varchar) FROM \"NhanVien\" WHERE \"maNhanVien\" = :userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return result != null ? result.toString() : null;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean isCustomer(String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"KhachHang\" WHERE \"maKhachHang\" = :userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            em.close();
        }
    }

    public String registerCustomer(Auth auth) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Lưu thông tin NguoiDung
            em.persist(auth);

            // 2. Lưu thông tin KhachHang
            em.createNativeQuery(
                    "INSERT INTO \"KhachHang\" (\"maKhachHang\") VALUES (:maKhachHang)")
                    .setParameter("maKhachHang", auth.getMaNguoiDung())
                    .executeUpdate();

            // 3. Tạo giỏ hàng rỗng cho khách hàng mới
            String cartId = "GH_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            em.createNativeQuery(
                    "INSERT INTO \"GioHang\" (\"maGioHang\", \"Deleted\", \"tongTien\", \"maKhachHang\") VALUES (:cartId, false, 0, :maKhachHang)")
                    .setParameter("cartId", cartId)
                    .setParameter("maKhachHang", auth.getMaNguoiDung())
                    .executeUpdate();

            tx.commit();
            return auth.getMaNguoiDung();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi lưu thông tin khách hàng và giỏ hàng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void updateRefreshToken(String userId, String refreshToken) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Auth auth = em.find(Auth.class, userId);
            if (auth != null) {
                auth.setRefreshToken(refreshToken);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật Refresh Token: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
