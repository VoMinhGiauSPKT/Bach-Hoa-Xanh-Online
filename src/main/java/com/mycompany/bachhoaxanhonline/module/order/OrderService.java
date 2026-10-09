package com.mycompany.bachhoaxanhonline.module.order;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.module.cart.CartResponse.LineItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private final OrderRepository orderRepository = new OrderRepository();

    public static class OrderException extends RuntimeException {
        private final int statusCode;
        public OrderException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        public int getStatusCode() { return statusCode; }
    }

    public ApiResponse<OrderResponse.OrderDetailData> createOrder(String customerId, OrderRequest.CreateOrderRequest request) {
        if (request.getTenNguoiNhan() == null || request.getSoDienThoaiNhan() == null || request.getDiaChiGiaoHang() == null) {
            throw new OrderException(400, "Thiếu thông tin địa chỉ giao hàng");
        }

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Order order = new Order();

        try {
            tx.begin();

            // 1. Get Cart
            Cart cart = em.createQuery("SELECT c FROM Cart c LEFT JOIN FETCH c.lineItems WHERE c.maKhachHang = :customerId", Cart.class)
                    .setParameter("customerId", customerId)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new OrderException(404, "Không tìm thấy giỏ hàng"));

            List<LineItem> itemsToOrder = new ArrayList<>();
            for (LineItem item : cart.getLineItems()) {
                if (item.getMaDonHang() == null) {
                    itemsToOrder.add(item);
                }
            }

            if (itemsToOrder.isEmpty()) {
                throw new OrderException(400, "Giỏ hàng rỗng, không thể đặt hàng");
            }

            // 2. Validate Stock and Deduct
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (LineItem item : itemsToOrder) {
                Product product = em.find(Product.class, item.getMaSanPham());
                if (product == null || product.getDeleted()) {
                    throw new OrderException(404, "Sản phẩm " + item.getMaSanPham() + " không còn tồn tại");
                }
                if (product.getSoLuong() < item.getSoLuong()) {
                    throw new OrderException(409, "Sản phẩm " + product.getTenSanPham() + " không đủ số lượng tồn kho");
                }
                product.setSoLuong(product.getSoLuong() - item.getSoLuong());
                em.merge(product);
                totalAmount = totalAmount.add(item.getThanhTien());
            }

            // 3. Create Order
            String orderId = UUID.randomUUID().toString();
            order.setMaDonHang(orderId);
            order.setMaKhachHang(customerId);
            order.setNgayLap(LocalDateTime.now());
            order.setTongTien(totalAmount);
            order.setTrangThai("CHUATHANHTOAN");
            order.setTenNguoiNhan(request.getTenNguoiNhan());
            order.setSoDienThoaiNhan(request.getSoDienThoaiNhan());
            order.setDiaChiGiaoHang(request.getDiaChiGiaoHang());
            
            // Set defaults for new NOT NULL columns in DB
            order.setDeleted(false);
            order.setNgayHetHanThanhToan(LocalDateTime.now().plusDays(3));
            order.setTienGiamGia(BigDecimal.ZERO);
            order.setTongTienSauGiamGia(totalAmount);

            em.persist(order);

            // 4. Update LineItems and Cart
            for (LineItem item : itemsToOrder) {
                item.setMaDonHang(orderId);
                em.merge(item);
            }
            cart.setTongTien(BigDecimal.ZERO);
            em.merge(cart);

            tx.commit();
        } catch (OrderException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new OrderException(500, "Lỗi khi tạo đơn hàng: " + e.getMessage());
        } finally {
            em.close();
        }

        return getOrderDetail(order.getMaDonHang(), customerId, "CUSTOMER");
    }

    public ApiResponse<OrderResponse.OrderDetailData> confirmCod(String orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new OrderException(404, "Không tìm thấy đơn hàng");
            }

            if (!"CHUATHANHTOAN".equals(order.getTrangThai())) {
                throw new OrderException(400, "Đơn hàng không ở trạng thái CHƯA THANH TOÁN");
            }

            order.setTrangThai("DATHANHTOAN");
            em.merge(order);

            Payment payment = new Payment();
            payment.setMaTT(UUID.randomUUID().toString());
            payment.setOrder(order);
            payment.setSoTien(order.getTongTien());
            payment.setTrangThai("THANHCONG");
            payment.setNgayThanhToan(LocalDateTime.now());
            em.persist(payment);

            tx.commit();
        } catch (OrderException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new OrderException(500, "Lỗi hệ thống: " + e.getMessage());
        } finally {
            em.close();
        }

        return getOrderDetail(orderId, null, "STAFF");
    }

    public ApiResponse<OrderResponse.OrderListData> getOrders(String customerId, String role, int page, int limit) {
        List<Order> orders;
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            orders = orderRepository.findByCustomerId(customerId, page, limit);
        } else {
            orders = orderRepository.findAll(page, limit);
        }

        OrderResponse.OrderListData data = new OrderResponse.OrderListData();
        data.setCurrentPage(page);
        
        List<OrderResponse.OrderSummary> summaries = new ArrayList<>();
        for (Order o : orders) {
            OrderResponse.OrderSummary summary = new OrderResponse.OrderSummary();
            summary.setMaDonHang(o.getMaDonHang());
            summary.setNgayLap(o.getNgayLap());
            summary.setTongTien(o.getTongTien());
            summary.setTrangThai(o.getTrangThai());
            summary.setSoLuongMatHang(o.getLineItems() != null ? o.getLineItems().size() : 0);
            summaries.add(summary);
        }
        data.setItems(summaries);

        return new ApiResponse<>(200, "Lấy danh sách đơn hàng thành công", data);
    }

    public ApiResponse<OrderResponse.OrderDetailData> getOrderDetail(String orderId, String customerId, String role) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Order order = em.createQuery("SELECT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE o.maDonHang = :orderId", Order.class)
                    .setParameter("orderId", orderId)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new OrderException(404, "Không tìm thấy đơn hàng"));

            if ("CUSTOMER".equalsIgnoreCase(role) && !order.getMaKhachHang().equals(customerId)) {
                throw new OrderException(403, "Bạn không có quyền xem đơn hàng này");
            }

            OrderResponse.OrderDetailData data = new OrderResponse.OrderDetailData();
            data.setMaDonHang(order.getMaDonHang());
            data.setMaKhachHang(order.getMaKhachHang());
            data.setNgayLap(order.getNgayLap());
            data.setTongTien(order.getTongTien());
            data.setTrangThai(order.getTrangThai());
            data.setPhuongThucTT("COD"); // Mặc định là COD vì CSDL không lưu trường này
            data.setTenNguoiNhan(order.getTenNguoiNhan());
            data.setSoDienThoaiNhan(order.getSoDienThoaiNhan());
            data.setDiaChiGiaoHang(order.getDiaChiGiaoHang());

            List<LineItemData> itemDataList = new ArrayList<>();
            for (LineItem item : order.getLineItems()) {
                LineItemData itemData = new LineItemData();
                itemData.setLineItemId(item.getLineItemId());
                itemData.setProductId(item.getMaSanPham());
                itemData.setQuantity(item.getSoLuong());
                itemData.setItemTotal(item.getThanhTien());
                
                Product p = em.find(Product.class, item.getMaSanPham());
                if (p != null) {
                    itemData.setProductName(p.getTenSanPham());
                    itemData.setImageUrl(p.getHinhAnh());
                    itemData.setUnit(p.getDonViTinh());
                    itemData.setPrice(p.getGiaBan());
                }
                itemDataList.add(itemData);
            }
            data.setItems(itemDataList);

            return new ApiResponse<>(200, "Lấy chi tiết đơn hàng thành công", data);
        } finally {
            em.close();
        }
    }

    public ApiResponse<String> createPaymentLink(String orderId, String customerId, String role) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order order = em.find(Order.class, orderId);
            
            if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
                throw new OrderException(404, "Không tìm thấy đơn hàng");
            }

            if ("CUSTOMER".equalsIgnoreCase(role) && !order.getMaKhachHang().equals(customerId)) {
                throw new OrderException(403, "Bạn không có quyền thanh toán cho đơn hàng này");
            }

            if (!"CHUATHANHTOAN".equals(order.getTrangThai())) {
                throw new OrderException(400, "Đơn hàng không ở trạng thái CHƯA THANH TOÁN");
            }

            if (order.getNgayHetHanThanhToan() != null && order.getNgayHetHanThanhToan().isBefore(LocalDateTime.now())) {
                throw new OrderException(400, "Đơn hàng đã hết hạn thanh toán");
            }

            long orderCode = System.currentTimeMillis() % 100000000000L + Math.abs((long) orderId.hashCode() % 10000);
            
            order.setGhiChu(String.valueOf(orderCode));
            em.merge(order);

            BigDecimal finalAmount = (order.getTongTienSauGiamGia() != null && order.getTongTienSauGiamGia().compareTo(BigDecimal.ZERO) > 0)
                    ? order.getTongTienSauGiamGia()
                    : order.getTongTien();

            Payment payment = new Payment();
            payment.setMaTT(String.valueOf(orderCode));
            payment.setOrder(order);
            payment.setSoTien(finalAmount);
            payment.setTrangThai("DANGXULY");
            payment.setNgayThanhToan(LocalDateTime.now());
            em.persist(payment);
            
            tx.commit();

            String returnUrl = com.mycompany.bachhoaxanhonline.util.ConfigUtil.get("PAYOS_RETURN_URL", "http://localhost:8080/BachHoaXanhOnline/");
            String cancelUrl = com.mycompany.bachhoaxanhonline.util.ConfigUtil.get("PAYOS_CANCEL_URL", "http://localhost:8080/BachHoaXanhOnline/");

            vn.payos.type.PaymentData paymentData = vn.payos.type.PaymentData.builder()
                .orderCode(orderCode)
                .amount(finalAmount.intValue())
                .description("Thanh toan don hang")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .buyerName(order.getTenNguoiNhan())
                .buyerPhone(order.getSoDienThoaiNhan())
                .buyerAddress(order.getDiaChiGiaoHang())
                .build();

            vn.payos.type.CheckoutResponseData data = com.mycompany.bachhoaxanhonline.config.PayOSConfig.getPayOS().createPaymentLink(paymentData);
            return new ApiResponse<>(200, "Tạo link thanh toán thành công", data.getCheckoutUrl());
        } catch (OrderException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new OrderException(500, "Lỗi gọi PayOS: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
