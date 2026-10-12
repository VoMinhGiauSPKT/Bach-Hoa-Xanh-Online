package com.mycompany.bachhoaxanhonline.module.order;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class OrderRepository {

    public List<Order> findByCustomerId(String customerId, int page, int limit) {
        return findByCustomerId(customerId, page, limit, null, null);
    }

    public List<Order> findByCustomerId(String customerId, int page, int limit, String status, String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE o.maKhachHang = :maKhachHang AND (o.deleted IS NULL OR o.deleted = false)");
            if (status != null && !status.trim().isEmpty()) {
                jpql.append(" AND o.trangThai = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND (LOWER(o.maDonHang) LIKE :kw OR LOWER(o.tenNguoiNhan) LIKE :kw OR o.soDienThoaiNhan LIKE :kw)");
            }
            jpql.append(" ORDER BY o.ngayLap DESC");

            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class)
                    .setParameter("maKhachHang", customerId);
            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim().toUpperCase());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }

            int offset = Math.max(0, (page - 1) * limit);
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Order> findAll(int page, int limit) {
        return findAll(page, limit, null, null);
    }

    public List<Order> findAll(int page, int limit, String status, String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false)");
            if (status != null && !status.trim().isEmpty()) {
                jpql.append(" AND o.trangThai = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND (LOWER(o.maDonHang) LIKE :kw OR LOWER(o.tenNguoiNhan) LIKE :kw OR o.soDienThoaiNhan LIKE :kw)");
            }
            jpql.append(" ORDER BY o.ngayLap DESC");

            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);
            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim().toUpperCase());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }

            int offset = Math.max(0, (page - 1) * limit);
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public long countAll() {
        return countAll(null, null);
    }

    public long countAll(String status, String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(o) FROM Order o WHERE (o.deleted IS NULL OR o.deleted = false)");
            if (status != null && !status.trim().isEmpty()) {
                jpql.append(" AND o.trangThai = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND (LOWER(o.maDonHang) LIKE :kw OR LOWER(o.tenNguoiNhan) LIKE :kw OR o.soDienThoaiNhan LIKE :kw)");
            }
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim().toUpperCase());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            em.close();
        }
    }

    public long countByCustomerId(String customerId) {
        return countByCustomerId(customerId, null, null);
    }

    public long countByCustomerId(String customerId, String status, String keyword) {
        if (customerId == null) return 0L;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(o) FROM Order o WHERE o.maKhachHang = :customerId AND (o.deleted IS NULL OR o.deleted = false)");
            if (status != null && !status.trim().isEmpty()) {
                jpql.append(" AND o.trangThai = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND (LOWER(o.maDonHang) LIKE :kw OR LOWER(o.tenNguoiNhan) LIKE :kw OR o.soDienThoaiNhan LIKE :kw)");
            }
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                    .setParameter("customerId", customerId);
            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim().toUpperCase());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            em.close();
        }
    }

    public Order findById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) return null;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Order.class, orderId.trim());
        } finally {
            em.close();
        }
    }

    public Order save(Order order) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(order);
            tx.commit();
            return order;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Order update(Order order) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order merged = em.merge(order);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void saveWithTransaction(EntityManager em, Order order) {
        em.persist(order);
    }
}
