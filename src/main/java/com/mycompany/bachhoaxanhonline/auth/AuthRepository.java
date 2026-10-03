package com.mycompany.bachhoaxanhonline.auth;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.Optional;
import java.util.UUID;

public class AuthRepository {

    public Optional<Auth> findByPhone(String soDienThoai) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Auth auth = em.createNamedQuery("Auth.findByPhone", Auth.class)
                    .setParameter("soDienThoai", soDienThoai)
                    .getSingleResult();
            return Optional.ofNullable(auth);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public boolean existsByPhone(String soDienThoai) {
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

    public boolean isStaff(String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NhanVien\" WHERE \"maNhanVien\" = :userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } catch (Exception e) {
            return false;
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

            // 1. Lưu bản ghi NguoiDung
            em.persist(auth);

            // 2. Tạo bản ghi KhachHang
            em.createNativeQuery(
                    "INSERT INTO \"KhachHang\" (\"maKhachHang\") VALUES (:maKhachHang)")
                    .setParameter("maKhachHang", auth.getMaNguoiDung())
                    .executeUpdate();

            // 3. Tạo 1 GioHang rỗng liên kết
            String cartId = "GH_" + UUID.randomUUID().toString().substring(0, 8);
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
