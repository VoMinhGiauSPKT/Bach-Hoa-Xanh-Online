package com.mycompany.bachhoaxanhonline.module.payment;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {

    public Payment save(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.find(Payment.class, payment.getMaTT()) != null) {
                payment = em.merge(payment);
            } else {
                em.persist(payment);
            }
            tx.commit();
            return payment;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findById(String maTT) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Payment.class, maTT));
        } finally {
            em.close();
        }
    }

    public List<Payment> findByOrderId(String orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createNamedQuery("Payment.findByOrderId", Payment.class)
                    .setParameter("maDonHang", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
