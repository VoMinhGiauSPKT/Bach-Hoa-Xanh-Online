package com.mycompany.bachhoaxanhonline.module.category;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    public List<Category> findAll(boolean onlyActive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            if (onlyActive) {
                return em.createNamedQuery("Category.findAllActive", Category.class).getResultList();
            } else {
                return em.createNamedQuery("Category.findAll", Category.class).getResultList();
            }
        } catch (Exception e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Category category = em.find(Category.class, id.trim());
            return Optional.ofNullable(category);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Category category = em.find(Category.class, id.trim());
            return category != null;
        } finally {
            em.close();
        }
    }

    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createNamedQuery("Category.existsByName", Long.class)
                    .setParameter("tenLoai", name.trim())
                    .getSingleResult();
            return count != null && count > 0;
        } catch (NoResultException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public boolean existsByNameExceptId(String name, String id) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createNamedQuery("Category.existsByNameAndNotId", Long.class)
                    .setParameter("tenLoai", name.trim())
                    .setParameter("maLoai", id.trim())
                    .getSingleResult();
            return count != null && count > 0;
        } catch (NoResultException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public long countActiveProductsByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return 0;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"SanPham\" WHERE \"maLoai\" = :maLoai AND \"Deleted\" = false")
                    .setParameter("maLoai", categoryId.trim())
                    .getSingleResult();
            return count != null ? count.longValue() : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public Category save(Category category) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(category);
            tx.commit();
            return category;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi lưu loại sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Category update(Category category) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Category merged = em.merge(category);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật loại sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean softDelete(String id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Category category = em.find(Category.class, id.trim());
            if (category == null) {
                tx.rollback();
                return false;
            }
            category.setDeleted(true);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi xóa loại sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
