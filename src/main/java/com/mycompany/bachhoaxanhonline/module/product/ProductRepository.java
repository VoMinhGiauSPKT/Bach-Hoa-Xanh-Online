package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public List<Product> findProducts(int page, int limit, String keyword, String categoryId, String sortBy, Boolean inStock) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.deleted = false");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.tenSanPham) LIKE :keyword");
            }
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                jpql.append(" AND p.maLoai = :categoryId");
            }
            if (inStock != null && inStock) {
                jpql.append(" AND p.soLuong > 0");
            }

            if ("price_asc".equalsIgnoreCase(sortBy)) {
                jpql.append(" ORDER BY p.giaBan ASC, p.maSanPham ASC");
            } else if ("price_desc".equalsIgnoreCase(sortBy)) {
                jpql.append(" ORDER BY p.giaBan DESC, p.maSanPham ASC");
            } else if ("newest".equalsIgnoreCase(sortBy)) {
                jpql.append(" ORDER BY p.maSanPham DESC");
            } else {
                jpql.append(" ORDER BY p.maSanPham ASC");
            }

            TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                query.setParameter("categoryId", categoryId.trim());
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

    public long countProducts(String keyword, String categoryId, Boolean inStock) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(p) FROM Product p WHERE p.deleted = false");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.tenSanPham) LIKE :keyword");
            }
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                jpql.append(" AND p.maLoai = :categoryId");
            }
            if (inStock != null && inStock) {
                jpql.append(" AND p.soLuong > 0");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                query.setParameter("categoryId", categoryId.trim());
            }

            Long count = query.getSingleResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            em.close();
        }
    }

    public Optional<Product> findById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Product product = em.find(Product.class, productId.trim());
            return Optional.ofNullable(product);
        } finally {
            em.close();
        }
    }

    public Product save(Product product) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(product);
            tx.commit();
            return product;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi lưu sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Product p = em.find(Product.class, productId.trim());
            return p != null;
        } finally {
            em.close();
        }
    }

    public boolean existsSupplierById(String supplierId) {
        return findSupplierNameById(supplierId) != null;
    }

    public int updateProductMetadata(String productId, String productName, String imageUrl,
                                     String categoryId, String supplierId, String unit, LocalDate expiryDate) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int updated = em.createQuery(
                    "UPDATE Product p SET "
                    + "p.tenSanPham = :tenSanPham, "
                    + "p.hinhAnh = :hinhAnh, "
                    + "p.maLoai = :maLoai, "
                    + "p.maNhaCungCap = :maNhaCungCap, "
                    + "p.donViTinh = :donViTinh, "
                    + "p.hanSuDung = :hanSuDung "
                    + "WHERE p.maSanPham = :productId AND p.deleted = false")
                    .setParameter("tenSanPham", productName)
                    .setParameter("hinhAnh", imageUrl)
                    .setParameter("maLoai", categoryId)
                    .setParameter("maNhaCungCap", supplierId)
                    .setParameter("donViTinh", unit)
                    .setParameter("hanSuDung", expiryDate)
                    .setParameter("productId", productId.trim())
                    .executeUpdate();
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean softDelete(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int updated = em.createQuery(
                    "UPDATE Product p SET p.deleted = true "
                    + "WHERE p.maSanPham = :id AND p.deleted = false")
                    .setParameter("id", productId.trim())
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi xóa mềm sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public static class RatingSummary {
        private final double ratingAverage;
        private final long totalReviews;

        public RatingSummary(double ratingAverage, long totalReviews) {
            this.ratingAverage = ratingAverage;
            this.totalReviews = totalReviews;
        }

        public double getRatingAverage() {
            return ratingAverage;
        }

        public long getTotalReviews() {
            return totalReviews;
        }
    }

    public Optional<Product> findActiveProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Product> list = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category c "
                    + "WHERE p.maSanPham = :id AND p.deleted = false", Product.class)
                    .setParameter("id", productId.trim())
                    .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public String findSupplierNameById(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return null;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Object result = em.createNativeQuery(
                    "SELECT \"tenNhaCungCap\" FROM \"NhaCungCap\" WHERE \"maNhaCungCap\" = :supplierId")
                    .setParameter("supplierId", supplierId.trim())
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

    public RatingSummary getRatingSummary(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return new RatingSummary(0.0, 0L);
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Object[] result = (Object[]) em.createNativeQuery(
                    "SELECT COALESCE(AVG(CAST(\"soSao\" AS double precision)), 0.0), COUNT(*) "
                    + "FROM \"DanhGia\" WHERE \"maSanPham\" = :productId AND \"Deleted\" = false")
                    .setParameter("productId", productId.trim())
                    .getSingleResult();
            if (result != null && result.length >= 2) {
                double avg = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
                long count = result[1] != null ? ((Number) result[1]).longValue() : 0L;
                double roundedAvg = Math.round(avg * 10.0) / 10.0;
                return new RatingSummary(roundedAvg, count);
            }
            return new RatingSummary(0.0, 0L);
        } catch (Exception e) {
            return new RatingSummary(0.0, 0L);
        } finally {
            em.close();
        }
    }
}
