package com.mycompany.bachhoaxanhonline.module.payment;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.config.PayOSConfig;
import com.mycompany.bachhoaxanhonline.entity.Order;
import com.mycompany.bachhoaxanhonline.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentService {

    public ApiResponse<String> processWebhook(Webhook webhookBody) {
        try {
            // Xác thực chữ ký webhook để đảm bảo dữ liệu gửi từ PayOS không bị giả mạo
            WebhookData data = PayOSConfig.getPayOS().verifyPaymentWebhookData(webhookBody);

            boolean isSuccess = "00".equals(data.getCode()) 
                || "00".equals(webhookBody.getCode()) 
                || Boolean.TRUE.equals(webhookBody.getSuccess())
                || "success".equalsIgnoreCase(data.getDesc());

            if (isSuccess) {
                Long orderCodeObj = data.getOrderCode();
                if (orderCodeObj == null) {
                    return new ApiResponse<>(200, "Webhook processed without orderCode", "OK");
                }
                long orderCode = orderCodeObj;
                
                EntityManager em = JpaUtil.getEntityManager();
                EntityTransaction tx = em.getTransaction();
                
                try {
                    tx.begin();
                    
                    // Thử tìm theo Payment (nơi maTT lưu trữ orderCode khi tạo link)
                    Payment payment = em.find(Payment.class, String.valueOf(orderCode));
                    Order order = null;
                    if (payment != null) {
                        order = payment.getOrder();
                    }
                    
                    // Fallback: Tìm qua ghiChu
                    if (order == null) {
                        order = em.createQuery("SELECT o FROM Order o WHERE o.ghiChu = :orderCode", Order.class)
                                .setParameter("orderCode", String.valueOf(orderCode))
                                .getResultStream()
                                .findFirst()
                                .orElse(null);
                    }
                            
                    if (order != null && "CHUATHANHTOAN".equals(order.getTrangThai())) {
                        order.setTrangThai("DATHANHTOAN");
                        em.merge(order);
                        
                        BigDecimal amountPaid = data.getAmount() != null 
                            ? BigDecimal.valueOf(data.getAmount()) 
                            : (order.getTongTienSauGiamGia() != null && order.getTongTienSauGiamGia().compareTo(BigDecimal.ZERO) > 0 
                                ? order.getTongTienSauGiamGia() : order.getTongTien());

                        if (payment != null) {
                            payment.setTrangThai("THANHCONG");
                            payment.setSoTien(amountPaid);
                            payment.setNgayThanhToan(LocalDateTime.now());
                            em.merge(payment);
                        } else {
                            payment = new Payment();
                            payment.setMaTT(String.valueOf(orderCode));
                            payment.setOrder(order);
                            payment.setSoTien(amountPaid);
                            payment.setTrangThai("THANHCONG");
                            payment.setNgayThanhToan(LocalDateTime.now());
                            em.persist(payment);
                        }
                    }
                    
                    tx.commit();
                } catch (Exception e) {
                    if (tx.isActive()) tx.rollback();
                    e.printStackTrace();
                    return new ApiResponse<>(500, "Lỗi cập nhật DB: " + e.getMessage(), null);
                } finally {
                    em.close();
                }
            }
            
            return new ApiResponse<>(200, "Webhook processed", "OK");
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(400, "Xác thực webhook thất bại: " + e.getMessage(), null);
        }
    }
}
