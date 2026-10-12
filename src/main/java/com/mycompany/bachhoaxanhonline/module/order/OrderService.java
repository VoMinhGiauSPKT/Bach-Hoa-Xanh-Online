package com.mycompany.bachhoaxanhonline.module.order;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.module.cart.CartResponse.LineItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import com.mycompany.bachhoaxanhonline.util.CodeGeneratorUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

            // 2.5. Xử lý áp dụng mã khuyến mãi (nếu có)
            BigDecimal discountAmount = BigDecimal.ZERO;
            String promoCode = request.getMaKhuyenMai();
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                promoCode = promoCode.trim().toUpperCase();
                List<Promotion> promos = em.createQuery(
                        "SELECT p FROM Promotion p WHERE UPPER(TRIM(p.maKhuyenMai)) = :code AND p.deleted = false", Promotion.class)
                        .setParameter("code", promoCode)
                        .getResultList();

                if (promos.isEmpty()) {
                    throw new OrderException(400, "Mã khuyến mãi (" + promoCode + ") không tồn tại hoặc đã bị xóa");
                }

                Promotion promo = promos.get(0);
                LocalDateTime now = LocalDateTime.now();

                if (promo.getNgayBatDau() != null && now.isBefore(promo.getNgayBatDau())) {
                    throw new OrderException(400, "Chương trình khuyến mãi (" + promoCode + ") chưa bắt đầu");
                }
                if (promo.getNgayKetThuc() != null && now.isAfter(promo.getNgayKetThuc())) {
                    throw new OrderException(400, "Mã khuyến mãi (" + promoCode + ") đã hết hạn sử dụng");
                }
                if (promo.getSoLuongDung() != null && promo.getSoLuongDung() <= 0) {
                    throw new OrderException(400, "Mã khuyến mãi (" + promoCode + ") đã hết lượt sử dụng");
                }

                BigDecimal minOrder = promo.getDonHangToiThieu() != null ? promo.getDonHangToiThieu() : BigDecimal.ZERO;
                if (totalAmount.compareTo(minOrder) < 0) {
                    throw new OrderException(400, String.format("Đơn hàng chưa đạt giá trị tối thiểu %,.0fđ để áp dụng voucher", minOrder.doubleValue()));
                }

                String pType = promo.getLoaiKhuyenMai();
                if ("TIENMAT".equalsIgnoreCase(pType)) {
                    discountAmount = promo.getGiaTriGiam() != null ? promo.getGiaTriGiam() : BigDecimal.ZERO;
                } else if ("PHANTRAM".equalsIgnoreCase(pType)) {
                    double percent = promo.getGiaTriGiam() != null ? promo.getGiaTriGiam().doubleValue() : 0.0;
                    discountAmount = totalAmount.multiply(BigDecimal.valueOf(percent / 100.0));
                    if (promo.getGiamToiDa() != null && promo.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                        if (discountAmount.compareTo(promo.getGiamToiDa()) > 0) {
                            discountAmount = promo.getGiamToiDa();
                        }
                    }
                }

                // Tiền giảm không vượt quá tổng giá trị đơn hàng
                if (discountAmount.compareTo(totalAmount) > 0) {
                    discountAmount = totalAmount;
                }

                // Trừ 1 lượt dùng của voucher
                em.createNativeQuery("UPDATE \"KhuyenMai\" SET \"soLuongDung\" = GREATEST(0, \"soLuongDung\" - 1) WHERE UPPER(TRIM(\"maKhuyenMai\")) = UPPER(TRIM(:pCode))")
                        .setParameter("pCode", promo.getMaKhuyenMai())
                        .executeUpdate();
            }

            // 3. Create Order
            String orderId = CodeGeneratorUtil.generateNextCode(em, "Order", "maDonHang", "HD", 4);
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
            order.setTienGiamGia(discountAmount);
            order.setTongTienSauGiamGia(totalAmount.subtract(discountAmount));
            String pMethod = request.getPhuongThucTT();
            if (pMethod == null || pMethod.trim().isEmpty()) {
                pMethod = request.getGhiChu();
            }
            if (pMethod == null || pMethod.trim().isEmpty()) {
                pMethod = "COD";
            }
            order.setGhiChu(pMethod);

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
            payment.setMaTT(CodeGeneratorUtil.generateNextCode(em, "Payment", "maTT", "TT", 4));
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
        long total;
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            orders = orderRepository.findByCustomerId(customerId, page, limit);
            total = orderRepository.countByCustomerId(customerId);
        } else {
            orders = orderRepository.findAll(page, limit);
            total = orderRepository.countAll();
        }

        OrderResponse.OrderListData data = new OrderResponse.OrderListData();
        data.setCurrentPage(page);
        data.setTotal(total);
        data.setTotalPages((int) Math.ceil((double) total / Math.max(1, limit)));
        
        List<OrderResponse.OrderSummary> summaries = new ArrayList<>();
        for (Order o : orders) {
            OrderResponse.OrderSummary summary = new OrderResponse.OrderSummary();
            summary.setMaDonHang(o.getMaDonHang());
            summary.setNgayLap(o.getNgayLap());
            summary.setTongTien(o.getTongTien());
            summary.setTienGiamGia(o.getTienGiamGia() != null ? o.getTienGiamGia() : BigDecimal.ZERO);
            summary.setTongTienSauGiamGia(o.getTongTienSauGiamGia() != null ? o.getTongTienSauGiamGia() : o.getTongTien());
            summary.setTrangThai(o.getTrangThai());
            summary.setPhuongThucTT(resolvePaymentMethod(o));
            int itemCount = 0;
            try {
                if (o.getLineItems() != null) {
                    itemCount = o.getLineItems().size();
                }
            } catch (Exception ignored) {
                itemCount = 0;
            }
            summary.setSoLuongMatHang(itemCount);
            summaries.add(summary);
        }
        data.setItems(summaries);

        return new ApiResponse<>(200, "Lấy danh sách đơn hàng thành công", data);
    }

    private String resolvePaymentMethod(Order o) {
        if (o == null) return "COD";
        String note = o.getGhiChu();
        if (note != null && !note.trim().isEmpty()) {
            String trimmed = note.trim();
            // Nếu ghi chú là dãy số (orderCode PayOS ví dụ 91646512293) hoặc là VIETQR / PAYOS
            if (trimmed.matches("\\d+") || "VIETQR".equalsIgnoreCase(trimmed) || "PAYOS".equalsIgnoreCase(trimmed)) {
                return "VIETQR";
            }
            if ("COD".equalsIgnoreCase(trimmed)) {
                return "COD";
            }
        }
        return "COD";
    }

    public ApiResponse<OrderResponse.OrderDetailData> getOrderDetail(String orderId, String customerId, String role) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Order order = em.createQuery("SELECT o FROM Order o WHERE o.maDonHang = :orderId", Order.class)
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
            data.setTienGiamGia(order.getTienGiamGia() != null ? order.getTienGiamGia() : BigDecimal.ZERO);
            data.setTongTienSauGiamGia(order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien());
            data.setTrangThai(order.getTrangThai());
            data.setPhuongThucTT(resolvePaymentMethod(order));
            data.setTenNguoiNhan(order.getTenNguoiNhan());
            data.setSoDienThoaiNhan(order.getSoDienThoaiNhan());
            data.setDiaChiGiaoHang(order.getDiaChiGiaoHang());

            List<LineItemData> itemDataList = new ArrayList<>();
            List<LineItem> lineItems = em.createQuery("SELECT l FROM LineItem l WHERE l.maDonHang = :orderId", LineItem.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
            for (LineItem item : lineItems) {
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

    /**
     * @deprecated Chuyển sang sử dụng PaymentService.createPaymentLink để đúng chuẩn phân chia trách nhiệm module.
     */
    @Deprecated
    public ApiResponse<String> createPaymentLink(String orderId, String customerId, String role) {
        com.mycompany.bachhoaxanhonline.module.payment.PaymentService paymentService = new com.mycompany.bachhoaxanhonline.module.payment.PaymentService();
        com.mycompany.bachhoaxanhonline.module.payment.PaymentResponse.PaymentLinkData data = paymentService.createPaymentLink(orderId, customerId, role).getData();
        return new ApiResponse<>(200, "Tạo link thanh toán thành công", data != null ? data.getCheckoutUrl() : null);
    }
}
