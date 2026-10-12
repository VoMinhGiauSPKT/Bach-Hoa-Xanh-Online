package com.mycompany.bachhoaxanhonline.module.supplier;

import com.mycompany.bachhoaxanhonline.entity.Supplier;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SupplierRepository {

    public List<Supplier> findAll(boolean activeOnly) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String queryName = activeOnly ? "Supplier.findAllActive" : "Supplier.findAll";
            TypedQuery<Supplier> query = em.createNamedQuery(queryName, Supplier.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Supplier> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Supplier s = em.find(Supplier.class, id.trim());
            return Optional.ofNullable(s);
        } catch (Exception e) {
            return Optional.empty();
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
            Long count = em.createQuery(
                    "SELECT COUNT(s) FROM Supplier s WHERE UPPER(TRIM(s.maNhaCungCap)) = UPPER(TRIM(:id))", Long.class)
                    .setParameter("id", id.trim())
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
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
            Long count = em.createNamedQuery("Supplier.existsByName", Long.class)
                    .setParameter("tenNhaCungCap", name.trim())
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
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
            Long count = em.createNamedQuery("Supplier.existsByNameAndNotId", Long.class)
                    .setParameter("tenNhaCungCap", name.trim())
                    .setParameter("maNhaCungCap", id.trim())
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        } finally {
            em.close();
        }
    }

    public void save(Supplier supplier) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(supplier);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(Supplier supplier) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(supplier);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void softDelete(String id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Supplier s = em.find(Supplier.class, id);
            if (s != null) {
                s.setDeleted(true);
                em.merge(s);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
