package com.mycompany.bachhoaxanhonline.module.payment;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.Order;
import com.mycompany.bachhoaxanhonline.entity.Payment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTest {

    @Test
    public void testPaymentLinkDataDto() {
        PaymentResponse.PaymentLinkData data = new PaymentResponse.PaymentLinkData(
                "DH001",
                123456789L,
                "https://pay.payos.vn/web/test123",
                "0002010102123857...",
                "970422",
                "123456789",
                "NGUYEN VAN A",
                50000,
                "DANGXULY"
        );

        Assertions.assertEquals("DH001", data.getOrderId());
        Assertions.assertEquals(123456789L, data.getOrderCode());
        Assertions.assertEquals("https://pay.payos.vn/web/test123", data.getCheckoutUrl());
        Assertions.assertEquals("0002010102123857...", data.getQrCode());
        Assertions.assertEquals("970422", data.getBin());
        Assertions.assertEquals("123456789", data.getAccountNumber());
        Assertions.assertEquals("NGUYEN VAN A", data.getAccountName());
        Assertions.assertEquals(50000, data.getAmount());
        Assertions.assertEquals("DANGXULY", data.getStatus());
    }

    @Test
    public void testPaymentServiceValidationOrderNotFound() {
        PaymentService paymentService = new PaymentService();
        PaymentService.PaymentException ex = Assertions.assertThrows(
                PaymentService.PaymentException.class,
                () -> paymentService.createPaymentLink("NON_EXISTENT_ORDER_ID_99999", "KH001", "CUSTOMER")
        );

        Assertions.assertEquals(404, ex.getStatusCode());
        Assertions.assertTrue(ex.getMessage().contains("Không tìm thấy đơn hàng"));
    }

    @Test
    public void testPaymentEntityGettersSetters() {
        Payment payment = new Payment();
        payment.setMaTT("TT_123");
        payment.setSoTien(BigDecimal.valueOf(100000));
        payment.setTrangThai("THANHCONG");
        payment.setDeleted(false);
        LocalDateTime now = LocalDateTime.now();
        payment.setNgayThanhToan(now);

        Order order = new Order();
        order.setMaDonHang("DH_999");
        payment.setOrder(order);

        Assertions.assertEquals("TT_123", payment.getMaTT());
        Assertions.assertEquals(BigDecimal.valueOf(100000), payment.getSoTien());
        Assertions.assertEquals("THANHCONG", payment.getTrangThai());
        Assertions.assertFalse(payment.getDeleted());
        Assertions.assertEquals(now, payment.getNgayThanhToan());
        Assertions.assertEquals("DH_999", payment.getOrder().getMaDonHang());
    }
}
