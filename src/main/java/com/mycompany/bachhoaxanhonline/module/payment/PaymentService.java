package com.mycompany.bachhoaxanhonline.module.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.config.PayOSConfig;
import com.mycompany.bachhoaxanhonline.entity.Order;
import com.mycompany.bachhoaxanhonline.entity.Payment;
import com.mycompany.bachhoaxanhonline.util.ConfigUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.payos.type.PaymentData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;
import vn.payos.util.SignatureUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class PaymentService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class PaymentException extends RuntimeException {
        private final int statusCode;

        public PaymentException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * Tạo link thanh toán PayOS cho đơn hàng
     */
    public ApiResponse<PaymentResponse.PaymentLinkData> createPaymentLink(String orderId, String customerId, String role) {
        EntityManager em = JpaUtil.getEntityManager();
        Order order;
        try {
            order = em.find(Order.class, orderId);
            if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
                throw new PaymentException(404, "Không tìm thấy đơn hàng");
            }

            if ("CUSTOMER".equalsIgnoreCase(role) && !order.getMaKhachHang().equals(customerId)) {
                throw new PaymentException(403, "Bạn không có quyền thanh toán cho đơn hàng này");
            }

            if (!"CHUATHANHTOAN".equalsIgnoreCase(order.getTrangThai()) && !"PENDING".equalsIgnoreCase(order.getTrangThai())) {
                throw new PaymentException(400, "Đơn hàng không ở trạng thái CHƯA THANH TOÁN");
            }

            if (order.getNgayHetHanThanhToan() != null && order.getNgayHetHanThanhToan().isBefore(LocalDateTime.now())) {
                throw new PaymentException(400, "Đơn hàng đã hết hạn thanh toán");
            }
        } finally {
            em.close();
        }

        // 1. Chuẩn bị thông tin thanh toán PayOS
        long orderCode = System.currentTimeMillis() % 100000000000L + Math.abs((long) orderId.hashCode() % 10000);

        BigDecimal finalAmount = (order.getTongTienSauGiamGia() != null && order.getTongTienSauGiamGia().compareTo(BigDecimal.ZERO) > 0)
                ? order.getTongTienSauGiamGia()
                : order.getTongTien();

        String clientId = ConfigUtil.get("PAYOS_CLIENT_ID");
        String apiKey = ConfigUtil.get("PAYOS_API_KEY");
        String checksumKey = ConfigUtil.get("PAYOS_CHECKSUM_KEY");
        String returnUrl = ConfigUtil.get("PAYOS_RETURN_URL", "http://localhost:8080/BachHoaXanhOnline/");
        String cancelUrl = ConfigUtil.get("PAYOS_CANCEL_URL", "http://localhost:8080/BachHoaXanhOnline/");

        if (clientId == null || apiKey == null || checksumKey == null) {
            throw new PaymentException(500, "Chưa cấu hình đầy đủ API Key PayOS trong hệ thống");
        }

        // Mô tả tối đa 25 ký tự theo quy định PayOS
        String shortId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
        String description = "Don hang " + shortId;

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(finalAmount.intValue())
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .buyerName(order.getTenNguoiNhan())
                .buyerPhone(order.getSoDienThoaiNhan())
                .buyerAddress(order.getDiaChiGiaoHang())
                .build();

        // 2. Gửi yêu cầu tạo link tới PayOS
        JsonNode dataNode;
        try {
            paymentData.setSignature(SignatureUtils.createSignatureOfPaymentRequest(paymentData, checksumKey));
            String jsonBody = objectMapper.writeValueAsString(paymentData);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-merchant.payos.vn/v2/payment-requests"))
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> httpRes = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpRes.body());

            if (!"00".equals(rootNode.path("code").asText())) {
                throw new PaymentException(400, "Lỗi từ cổng thanh toán PayOS: " + rootNode.path("desc").asText());
            }

            dataNode = rootNode.path("data");
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new PaymentException(500, "Lỗi kết nối tới cổng thanh toán: " + e.getMessage());
        }

        // 3. Khi PayOS đã sinh link thành công, ghi nhận vào CSDL an toàn
        EntityManager saveEm = JpaUtil.getEntityManager();
        EntityTransaction tx = saveEm.getTransaction();
        try {
            tx.begin();
            Order currentOrder = saveEm.find(Order.class, orderId);
            currentOrder.setGhiChu(String.valueOf(orderCode));
            saveEm.merge(currentOrder);

            Payment payment = saveEm.find(Payment.class, String.valueOf(orderCode));
            if (payment == null) {
                payment = new Payment();
                payment.setMaTT(String.valueOf(orderCode));
                payment.setOrder(currentOrder);
                payment.setSoTien(finalAmount);
                payment.setTrangThai("DANGXULY");
                payment.setNgayThanhToan(LocalDateTime.now());
                payment.setDeleted(false);
                saveEm.persist(payment);
            } else {
                payment.setSoTien(finalAmount);
                payment.setTrangThai("DANGXULY");
                payment.setNgayThanhToan(LocalDateTime.now());
                saveEm.merge(payment);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new PaymentException(500, "Lỗi ghi nhận giao dịch vào CSDL: " + e.getMessage());
        } finally {
            saveEm.close();
        }

        // 4. Trả về DTO chi tiết đầy đủ gồm checkoutUrl và qrCode cho FE
        PaymentResponse.PaymentLinkData responseData = new PaymentResponse.PaymentLinkData(
                orderId,
                orderCode,
                dataNode.path("checkoutUrl").asText(null),
                dataNode.path("qrCode").asText(null),
                dataNode.path("bin").asText(null),
                dataNode.path("accountNumber").asText(null),
                dataNode.path("accountName").asText(null),
                dataNode.path("amount").asInt(finalAmount.intValue()),
                "DANGXULY"
        );

        return new ApiResponse<>(200, "Tạo link thanh toán thành công", responseData);
    }

    /**
     * Xử lý Webhook (IPN) tự động từ PayOS khi khách thanh toán thành công
     */
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

                    // 1. Tìm Payment theo maTT (orderCode)
                    Payment payment = em.find(Payment.class, String.valueOf(orderCode));
                    Order order = null;
                    if (payment != null) {
                        order = payment.getOrder();
                    }

                    // 2. Fallback: Tìm qua ghiChu của đơn hàng
                    if (order == null) {
                        order = em.createQuery("SELECT o FROM Order o WHERE o.ghiChu = :orderCode", Order.class)
                                .setParameter("orderCode", String.valueOf(orderCode))
                                .getResultStream()
                                .findFirst()
                                .orElse(null);
                    }

                    if (order != null && ("CHUATHANHTOAN".equalsIgnoreCase(order.getTrangThai()) || "PENDING".equalsIgnoreCase(order.getTrangThai()))) {
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
                            payment.setDeleted(false);
                            em.persist(payment);
                        }
                    }

                    tx.commit();
                } catch (Exception e) {
                    if (tx.isActive()) tx.rollback();
                    e.printStackTrace();
                    return new ApiResponse<>(500, "Lỗi cập nhật CSDL: " + e.getMessage(), null);
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
