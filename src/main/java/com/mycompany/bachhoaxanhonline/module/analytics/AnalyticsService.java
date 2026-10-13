package com.mycompany.bachhoaxanhonline.module.analytics;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.entity.Category;
import com.mycompany.bachhoaxanhonline.entity.LineItem;
import com.mycompany.bachhoaxanhonline.entity.Order;
import com.mycompany.bachhoaxanhonline.entity.Product;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AnalyticsService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public ApiResponse<AnalyticsResponse.OverviewData> getOverview() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            AnalyticsResponse.OverviewData overview = new AnalyticsResponse.OverviewData();

            // 1. Lấy danh sách đơn hàng không bị xóa
            List<Order> orders = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false) ORDER BY o.ngayLap DESC",
                    Order.class
            ).getResultList();

            // Lấy trước map sản phẩm đang hoạt động để tra cứu giá nhập chuẩn xác
            Map<String, Product> productMap = getActiveProductMap(em);

            BigDecimal totalPaidRevenue = BigDecimal.ZERO;
            BigDecimal totalOriginalRevenue = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal totalCogs = BigDecimal.ZERO;
            long paidOrdersCount = 0L;
            long cancelledOrdersCount = 0L;
            long voucherUsedCount = 0L;

            for (Order order : orders) {
                String status = order.getTrangThai() != null ? order.getTrangThai().trim().toUpperCase() : "";

                if ("DATHANHTOAN".equals(status)) {
                    paidOrdersCount++;

                    BigDecimal paidAmount = order.getTongTienSauGiamGia() != null
                            ? order.getTongTienSauGiamGia()
                            : (order.getTongTien() != null ? order.getTongTien() : BigDecimal.ZERO);
                    totalPaidRevenue = totalPaidRevenue.add(paidAmount);

                    BigDecimal originalAmount = order.getTongTien() != null ? order.getTongTien() : BigDecimal.ZERO;
                    totalOriginalRevenue = totalOriginalRevenue.add(originalAmount);

                    BigDecimal discount = order.getTienGiamGia() != null ? order.getTienGiamGia() : BigDecimal.ZERO;
                    totalDiscount = totalDiscount.add(discount);

                    if ((order.getMaKhuyenMai() != null && !order.getMaKhuyenMai().trim().isEmpty())
                            || discount.compareTo(BigDecimal.ZERO) > 0) {
                        voucherUsedCount++;
                    }

                    // Tính giá vốn (COGS) cho đơn hàng thanh toán thành công
                    if (order.getLineItems() != null) {
                        for (LineItem item : order.getLineItems()) {
                            if (item.getDeleted() != null && item.getDeleted()) continue;
                            BigDecimal itemCost = calculateItemCost(item, productMap);
                            totalCogs = totalCogs.add(itemCost);
                        }
                    }
                } else if ("DAHUY".equals(status)) {
                    cancelledOrdersCount++;
                }
            }

            overview.setTotalOrders((long) orders.size());
            overview.setPaidOrdersCount(paidOrdersCount);
            overview.setCancelledOrdersCount(cancelledOrdersCount);
            overview.setVoucherUsedCount(voucherUsedCount);
            overview.setTotalPaidRevenue(totalPaidRevenue.setScale(0, RoundingMode.HALF_UP));
            overview.setTotalOriginalRevenue(totalOriginalRevenue.setScale(0, RoundingMode.HALF_UP));
            overview.setTotalDiscount(totalDiscount.setScale(0, RoundingMode.HALF_UP));
            overview.setTotalCogs(totalCogs.setScale(0, RoundingMode.HALF_UP));

            // Lãi thuần = Doanh thu thực nhận - Giá vốn hàng bán (COGS)
            BigDecimal netProfit = totalPaidRevenue.subtract(totalCogs);
            overview.setTotalNetProfit(netProfit.setScale(0, RoundingMode.HALF_UP));

            // Tỷ suất lợi nhuận (%)
            if (totalPaidRevenue.compareTo(BigDecimal.ZERO) > 0) {
                double margin = (netProfit.doubleValue() / totalPaidRevenue.doubleValue()) * 100.0;
                overview.setProfitMargin(Math.round(margin * 10.0) / 10.0);
            } else {
                overview.setProfitMargin(0.0);
            }

            // 2. Thống kê sản phẩm đang kinh doanh & cảnh báo kho
            List<Product> products = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE (p.deleted IS NULL OR p.deleted = false)",
                    Product.class
            ).getResultList();

            LocalDate today = LocalDate.now();
            long lowStock = 0L;
            long expiring = 0L;

            for (Product p : products) {
                if (p.getSoLuong() != null && p.getSoLuong() <= 10) {
                    lowStock++;
                }
                if (p.getHanSuDung() != null) {
                    long daysDiff = ChronoUnit.DAYS.between(today, p.getHanSuDung());
                    if (daysDiff <= 15) {
                        expiring++;
                    }
                }
            }

            overview.setTotalActiveProducts((long) products.size());
            overview.setLowStockCount(lowStock);
            overview.setExpiringCount(expiring);

            return new ApiResponse<>(200, "Lấy tổng quan báo cáo thành công", overview);
        } finally {
            em.close();
        }
    }

    public ApiResponse<AnalyticsResponse.ChartData> getChartData(String period, Integer daysParam) {
        String safePeriod = (period != null && !period.trim().isEmpty()) ? period.trim().toLowerCase() : "day";
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Map<String, Product> productMap = getActiveProductMap(em);
            List<Order> orders = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false) ORDER BY o.ngayLap ASC",
                    Order.class
            ).getResultList();

            LocalDate today = LocalDate.now();
            Map<String, AnalyticsResponse.ChartPoint> pointMap = new LinkedHashMap<>();

            if ("year".equals(safePeriod)) {
                // Nhóm theo năm (3 năm gần nhất)
                int currentYear = today.getYear();
                for (int y = currentYear - 2; y <= currentYear; y++) {
                    String key = String.valueOf(y);
                    pointMap.put(key, new AnalyticsResponse.ChartPoint("Năm " + y, key));
                }

                for (Order o : orders) {
                    if (o.getNgayLap() == null) continue;
                    String yKey = String.valueOf(o.getNgayLap().getYear());
                    AnalyticsResponse.ChartPoint point = pointMap.computeIfAbsent(yKey, k -> new AnalyticsResponse.ChartPoint("Năm " + k, k));
                    accumulateOrderToPoint(o, point, productMap);
                }
            } else if ("month".equals(safePeriod)) {
                // Nhóm theo 12 tháng gần nhất
                LocalDate startMonth = today.minusMonths(11).withDayOfMonth(1);
                for (int i = 0; i < 12; i++) {
                    LocalDate m = startMonth.plusMonths(i);
                    String key = m.format(MONTH_KEY_FORMATTER);
                    String label = m.format(MONTH_FORMATTER);
                    pointMap.put(key, new AnalyticsResponse.ChartPoint(label, key));
                }

                for (Order o : orders) {
                    if (o.getNgayLap() == null) continue;
                    String mKey = o.getNgayLap().format(MONTH_KEY_FORMATTER);
                    if (pointMap.containsKey(mKey)) {
                        accumulateOrderToPoint(o, pointMap.get(mKey), productMap);
                    }
                }
            } else {
                // Mặc định nhóm theo ngày (mặc định 7 ngày hoặc 30 ngày)
                int days = (daysParam != null && daysParam > 0) ? daysParam : 7;
                if (days > 90) days = 90;

                LocalDate startDate = today.minusDays(days - 1);
                for (int i = 0; i < days; i++) {
                    LocalDate d = startDate.plusDays(i);
                    String key = d.format(DATE_KEY_FORMATTER);
                    String label = d.format(DAY_FORMATTER);
                    pointMap.put(key, new AnalyticsResponse.ChartPoint(label, key));
                }

                for (Order o : orders) {
                    if (o.getNgayLap() == null) continue;
                    String dKey = o.getNgayLap().toLocalDate().format(DATE_KEY_FORMATTER);
                    if (pointMap.containsKey(dKey)) {
                        accumulateOrderToPoint(o, pointMap.get(dKey), productMap);
                    }
                }
            }

            // Tính lãi thuần cho từng point = revenue - cogs
            List<AnalyticsResponse.ChartPoint> points = new ArrayList<>();
            for (AnalyticsResponse.ChartPoint pt : pointMap.values()) {
                pt.setProfit(pt.getRevenue().subtract(pt.getCogs()).setScale(0, RoundingMode.HALF_UP));
                pt.setRevenue(pt.getRevenue().setScale(0, RoundingMode.HALF_UP));
                pt.setCogs(pt.getCogs().setScale(0, RoundingMode.HALF_UP));
                pt.setDiscountAmount(pt.getDiscountAmount().setScale(0, RoundingMode.HALF_UP));
                points.add(pt);
            }

            AnalyticsResponse.ChartData chartData = new AnalyticsResponse.ChartData(safePeriod, points);
            return new ApiResponse<>(200, "Lấy dữ liệu biểu đồ thành công", chartData);
        } finally {
            em.close();
        }
    }

    public ApiResponse<AnalyticsResponse.InventoryAlertsData> getInventoryAlerts() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Product> products = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE (p.deleted IS NULL OR p.deleted = false) ORDER BY p.soLuong ASC",
                    Product.class
            ).getResultList();

            LocalDate today = LocalDate.now();
            AnalyticsResponse.InventoryAlertsData data = new AnalyticsResponse.InventoryAlertsData();

            for (Product p : products) {
                // 1. Kiểm tra tồn kho
                if (p.getSoLuong() != null && p.getSoLuong() <= 10) {
                    AnalyticsResponse.InventoryAlertItem item = mapToAlertItem(p, today);
                    if (p.getSoLuong() == 0) {
                        item.setAlertType("OUT_OF_STOCK"); // Hết hàng
                    } else {
                        item.setAlertType("LOW_STOCK");    // Sắp hết hàng
                    }
                    data.getLowStockProducts().add(item);
                }

                // 2. Kiểm tra hạn sử dụng
                if (p.getHanSuDung() != null) {
                    long diff = ChronoUnit.DAYS.between(today, p.getHanSuDung());
                    if (diff <= 15) {
                        AnalyticsResponse.InventoryAlertItem item = mapToAlertItem(p, today);
                        if (diff < 0) {
                            item.setAlertType("EXPIRED");       // Đã hết hạn
                        } else {
                            item.setAlertType("EXPIRING_SOON"); // Sắp hết hạn trong 15 ngày
                        }
                        data.getExpiringProducts().add(item);
                    }
                }
            }

            // Sắp xếp danh sách sắp hết hạn: Quá hạn trước, sau đó tăng dần theo ngày còn lại
            data.getExpiringProducts().sort((a, b) -> {
                Long d1 = a.getDaysUntilExpiry() != null ? a.getDaysUntilExpiry() : 0L;
                Long d2 = b.getDaysUntilExpiry() != null ? b.getDaysUntilExpiry() : 0L;
                return d1.compareTo(d2);
            });

            return new ApiResponse<>(200, "Lấy danh sách cảnh báo kho thành công", data);
        } finally {
            em.close();
        }
    }

    public ApiResponse<List<AnalyticsResponse.TopProductItem>> getTopProducts(int limit) {
        if (limit <= 0) limit = 5;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Map<String, Product> productMap = getActiveProductMap(em);
            List<Order> orders = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false) AND UPPER(o.trangThai) = 'DATHANHTOAN'",
                    Order.class
            ).getResultList();

            Map<String, AnalyticsResponse.TopProductItem> topMap = new HashMap<>();

            for (Order o : orders) {
                if (o.getLineItems() == null) continue;
                for (LineItem item : o.getLineItems()) {
                    if (item.getDeleted() != null && item.getDeleted()) continue;
                    String pId = item.getMaSanPham();
                    if (pId == null) continue;

                    AnalyticsResponse.TopProductItem topItem = topMap.computeIfAbsent(pId, k -> {
                        AnalyticsResponse.TopProductItem newItem = new AnalyticsResponse.TopProductItem();
                        newItem.setProductId(k);
                        Product p = productMap.get(k);
                        if (p != null) {
                            newItem.setProductName(p.getTenSanPham());
                            newItem.setImageUrl(p.getHinhAnh());
                            newItem.setUnit(p.getDonViTinh());
                            newItem.setCategoryName(p.getCategory() != null ? p.getCategory().getTenLoaiSanPham() : "");
                        } else {
                            newItem.setProductName(k);
                        }
                        newItem.setSoldQuantity(0.0);
                        newItem.setRevenue(BigDecimal.ZERO);
                        newItem.setCogs(BigDecimal.ZERO);
                        newItem.setProfit(BigDecimal.ZERO);
                        return newItem;
                    });

                    double qty = item.getSoLuong() != null ? item.getSoLuong() : 0.0;
                    BigDecimal rev = item.getThanhTien() != null ? item.getThanhTien() : BigDecimal.ZERO;
                    BigDecimal cost = calculateItemCost(item, productMap);

                    topItem.setSoldQuantity(Math.round((topItem.getSoldQuantity() + qty) * 100.0) / 100.0);
                    topItem.setRevenue(topItem.getRevenue().add(rev));
                    topItem.setCogs(topItem.getCogs().add(cost));
                }
            }

            List<AnalyticsResponse.TopProductItem> result = new ArrayList<>(topMap.values());
            for (AnalyticsResponse.TopProductItem item : result) {
                item.setProfit(item.getRevenue().subtract(item.getCogs()).setScale(0, RoundingMode.HALF_UP));
                item.setRevenue(item.getRevenue().setScale(0, RoundingMode.HALF_UP));
                item.setCogs(item.getCogs().setScale(0, RoundingMode.HALF_UP));
            }

            // Sắp xếp theo số lượng bán giảm dần, nếu bằng nhau thì theo doanh thu
            result.sort((a, b) -> {
                int cmp = Double.compare(b.getSoldQuantity(), a.getSoldQuantity());
                if (cmp != 0) return cmp;
                return b.getRevenue().compareTo(a.getRevenue());
            });

            if (result.size() > limit) {
                result = result.subList(0, limit);
            }

            return new ApiResponse<>(200, "Lấy danh sách top sản phẩm thành công", result);
        } finally {
            em.close();
        }
    }

    private Map<String, Product> getActiveProductMap(EntityManager em) {
        List<Product> products = em.createQuery(
                "SELECT p FROM Product p LEFT JOIN FETCH p.category",
                Product.class
        ).getResultList();
        Map<String, Product> map = new HashMap<>();
        for (Product p : products) {
            map.put(p.getMaSanPham(), p);
        }
        return map;
    }

    private BigDecimal calculateItemCost(LineItem item, Map<String, Product> productMap) {
        if (item == null) return BigDecimal.ZERO;
        double qty = item.getSoLuong() != null ? item.getSoLuong() : 1.0;
        Product p = productMap.get(item.getMaSanPham());

        BigDecimal costPerUnit = BigDecimal.ZERO;
        if (p != null) {
            if (p.getGiaNhap() != null && p.getGiaNhap().compareTo(BigDecimal.ZERO) > 0) {
                costPerUnit = p.getGiaNhap();
            } else if (p.getGiaBan() != null) {
                // Fallback nếu giaNhap chưa set: dựa vào tỷ suất lợi nhuận định mức của Loại sản phẩm
                Category cat = p.getCategory();
                if (cat != null && cat.getPhanTramLoiNhuan() != null && cat.getPhanTramLoiNhuan() > 0) {
                    double markup = 1.0 + (cat.getPhanTramLoiNhuan() / 100.0);
                    costPerUnit = p.getGiaBan().divide(BigDecimal.valueOf(markup), 2, RoundingMode.HALF_UP);
                } else {
                    costPerUnit = p.getGiaBan().multiply(BigDecimal.valueOf(0.70));
                }
            }
        }
        return costPerUnit.multiply(BigDecimal.valueOf(qty));
    }

    private void accumulateOrderToPoint(Order o, AnalyticsResponse.ChartPoint point, Map<String, Product> productMap) {
        String status = o.getTrangThai() != null ? o.getTrangThai().trim().toUpperCase() : "";
        if ("DATHANHTOAN".equals(status)) {
            point.setOrderCount(point.getOrderCount() + 1);

            BigDecimal paid = o.getTongTienSauGiamGia() != null
                    ? o.getTongTienSauGiamGia()
                    : (o.getTongTien() != null ? o.getTongTien() : BigDecimal.ZERO);
            point.setRevenue(point.getRevenue().add(paid));

            BigDecimal discount = o.getTienGiamGia() != null ? o.getTienGiamGia() : BigDecimal.ZERO;
            point.setDiscountAmount(point.getDiscountAmount().add(discount));

            if ((o.getMaKhuyenMai() != null && !o.getMaKhuyenMai().trim().isEmpty())
                    || discount.compareTo(BigDecimal.ZERO) > 0) {
                point.setVoucherCount(point.getVoucherCount() + 1);
            }

            if (o.getLineItems() != null) {
                for (LineItem item : o.getLineItems()) {
                    if (item.getDeleted() != null && item.getDeleted()) continue;
                    point.setCogs(point.getCogs().add(calculateItemCost(item, productMap)));
                }
            }
        } else if ("DAHUY".equals(status)) {
            point.setCancelledCount(point.getCancelledCount() + 1);
        }
    }

    private AnalyticsResponse.InventoryAlertItem mapToAlertItem(Product p, LocalDate today) {
        AnalyticsResponse.InventoryAlertItem item = new AnalyticsResponse.InventoryAlertItem();
        item.setProductId(p.getMaSanPham());
        item.setProductName(p.getTenSanPham());
        item.setImageUrl(p.getHinhAnh());
        item.setUnit(p.getDonViTinh());
        item.setPrice(p.getGiaBan());
        item.setCostPrice(p.getGiaNhap());
        item.setStock(p.getSoLuong());
        item.setCategoryName(p.getCategory() != null ? p.getCategory().getTenLoaiSanPham() : "");
        item.setExpiryDate(p.getHanSuDung());
        if (p.getHanSuDung() != null) {
            item.setDaysUntilExpiry(ChronoUnit.DAYS.between(today, p.getHanSuDung()));
        }
        return item;
    }
}
