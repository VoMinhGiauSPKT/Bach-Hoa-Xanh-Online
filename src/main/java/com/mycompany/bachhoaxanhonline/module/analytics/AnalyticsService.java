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
        return getOverview("week");
    }

    public ApiResponse<AnalyticsResponse.OverviewData> getOverview(String period) {
        String safePeriod = (period != null && !period.trim().isEmpty()) ? period.trim().toLowerCase() : "week";
        EntityManager em = JpaUtil.getEntityManager();
        try {
            AnalyticsResponse.OverviewData overview = new AnalyticsResponse.OverviewData();
            LocalDate today = LocalDate.now();

            List<Order> orders = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false) ORDER BY o.ngayLap DESC",
                    Order.class
            ).getResultList();

            Map<String, Product> productMap = getActiveProductMap(em);

            BigDecimal totalPaidRevenue = BigDecimal.ZERO;
            BigDecimal totalOriginalRevenue = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal totalCogs = BigDecimal.ZERO;
            long totalOrdersCount = 0L;
            long paidOrdersCount = 0L;
            long cancelledOrdersCount = 0L;
            long voucherUsedCount = 0L;

            for (Order order : orders) {
                if (!isOrderInPeriod(order, safePeriod, today)) continue;

                totalOrdersCount++;
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

            overview.setTotalOrders(totalOrdersCount);
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
        String safePeriod = (period != null && !period.trim().isEmpty()) ? period.trim().toLowerCase() : "week";
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Map<String, Product> productMap = getActiveProductMap(em);
            List<Order> orders = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false) ORDER BY o.ngayLap ASC",
                    Order.class
            ).getResultList();

            LocalDate today = LocalDate.now();
            Map<String, AnalyticsResponse.ChartPoint> pointMap = new LinkedHashMap<>();

            if ("today".equals(safePeriod) || "day".equals(safePeriod)) {
                // Hôm nay: Hiển thị 24 khung giờ (00h -> 23h)
                for (int h = 0; h < 24; h++) {
                    String key = String.format("%02d:00", h);
                    String label = String.format("%02dh", h);
                    pointMap.put(key, new AnalyticsResponse.ChartPoint(label, "Khung giờ " + key));
                }

                for (Order o : orders) {
                    if (o.getNgayLap() == null) continue;
                    if (!o.getNgayLap().toLocalDate().isEqual(today)) continue;
                    int h = o.getNgayLap().getHour();
                    String hKey = String.format("%02d:00", h);
                    if (pointMap.containsKey(hKey)) {
                        accumulateOrderToPoint(o, pointMap.get(hKey), productMap);
                    }
                }
            } else if ("month".equals(safePeriod)) {
                // Tháng này: Hiển thị các ngày từ ngày 01 đến ngày hôm nay của tháng hiện tại
                int currentDay = today.getDayOfMonth();
                for (int d = 1; d <= currentDay; d++) {
                    LocalDate date = today.withDayOfMonth(d);
                    String key = date.format(DATE_KEY_FORMATTER);
                    String label = date.format(DAY_FORMATTER);
                    pointMap.put(key, new AnalyticsResponse.ChartPoint(label, key));
                }

                for (Order o : orders) {
                    if (o.getNgayLap() == null) continue;
                    if (o.getNgayLap().getYear() != today.getYear() || o.getNgayLap().getMonthValue() != today.getMonthValue()) continue;
                    String dKey = o.getNgayLap().toLocalDate().format(DATE_KEY_FORMATTER);
                    if (pointMap.containsKey(dKey)) {
                        accumulateOrderToPoint(o, pointMap.get(dKey), productMap);
                    }
                }
            } else if ("year".equals(safePeriod)) {
                // Năm nay: Hiển thị 12 tháng của năm hiện tại
                int currentYear = today.getYear();
                for (int m = 1; m <= 12; m++) {
                    String key = String.format("%d-%02d", currentYear, m);
                    String label = "Thg " + m;
                    pointMap.put(key, new AnalyticsResponse.ChartPoint(label, "Tháng " + m + "/" + currentYear));
                }

                for (Order o : orders) {
                    if (o.getNgayLap() == null || o.getNgayLap().getYear() != currentYear) continue;
                    String mKey = String.format("%d-%02d", currentYear, o.getNgayLap().getMonthValue());
                    if (pointMap.containsKey(mKey)) {
                        accumulateOrderToPoint(o, pointMap.get(mKey), productMap);
                    }
                }
            } else {
                // Mặc định: Theo tuần (7 ngày gần nhất)
                LocalDate startDate = today.minusDays(6);
                for (int i = 0; i < 7; i++) {
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
        return getTopProducts(limit, "week");
    }

    public ApiResponse<List<AnalyticsResponse.TopProductItem>> getTopProducts(int limit, String period) {
        if (limit <= 0) limit = 5;
        String safePeriod = (period != null && !period.trim().isEmpty()) ? period.trim().toLowerCase() : "week";
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Map<String, Product> productMap = getActiveProductMap(em);
            LocalDate today = LocalDate.now();
            List<Order> orders = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lineItems WHERE (o.deleted IS NULL OR o.deleted = false) AND UPPER(o.trangThai) = 'DATHANHTOAN'",
                    Order.class
            ).getResultList();

            Map<String, AnalyticsResponse.TopProductItem> topMap = new HashMap<>();

            for (Order o : orders) {
                if (!isOrderInPeriod(o, safePeriod, today)) continue;
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

    private boolean isOrderInPeriod(Order o, String safePeriod, LocalDate today) {
        if (o == null || o.getNgayLap() == null) return false;
        LocalDate orderDate = o.getNgayLap().toLocalDate();
        switch (safePeriod) {
            case "today":
            case "day":
                return orderDate.isEqual(today);
            case "week":
                return !orderDate.isBefore(today.minusDays(6)) && !orderDate.isAfter(today);
            case "month":
                return !orderDate.isBefore(today.withDayOfMonth(1)) && !orderDate.isAfter(today);
            case "year":
                return orderDate.getYear() == today.getYear();
            case "all":
            default:
                return true;
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
