package com.mycompany.bachhoaxanhonline.module.analytics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsResponse {

    public static class OverviewData implements Serializable {
        private static final long serialVersionUID = 1L;

        private BigDecimal totalPaidRevenue = BigDecimal.ZERO;      // Doanh thu thực nhận (sau khi trừ voucher)
        private BigDecimal totalOriginalRevenue = BigDecimal.ZERO;  // Doanh thu gốc (trước giảm giá)
        private BigDecimal totalDiscount = BigDecimal.ZERO;         // Tổng tiền giảm giá (voucher)
        private BigDecimal totalCogs = BigDecimal.ZERO;             // Tổng giá vốn hàng bán (COGS)
        private BigDecimal totalNetProfit = BigDecimal.ZERO;        // Lãi thuần = Doanh thu thực - COGS
        private Double profitMargin = 0.0;                          // Tỷ suất lợi nhuận (%) = (Lãi thuần / Doanh thu thực) * 100

        private Long totalOrders = 0L;                              // Tổng đơn hàng đã phát sinh
        private Long paidOrdersCount = 0L;                          // Số đơn hoàn tất (DATHANHTOAN)
        private Long cancelledOrdersCount = 0L;                     // Số đơn đã huỷ (DAHUY)
        private Long voucherUsedCount = 0L;                         // Số đơn hàng có áp dụng mã giảm giá

        private Long totalActiveProducts = 0L;                      // Tổng sản phẩm đang kinh doanh
        private Long lowStockCount = 0L;                            // Số sản phẩm sắp hết hàng (<= 10)
        private Long expiringCount = 0L;                            // Số sản phẩm sắp hết hạn (<= 15 ngày hoặc quá hạn)

        public OverviewData() {}

        public BigDecimal getTotalPaidRevenue() { return totalPaidRevenue; }
        public void setTotalPaidRevenue(BigDecimal totalPaidRevenue) { this.totalPaidRevenue = totalPaidRevenue; }

        public BigDecimal getTotalOriginalRevenue() { return totalOriginalRevenue; }
        public void setTotalOriginalRevenue(BigDecimal totalOriginalRevenue) { this.totalOriginalRevenue = totalOriginalRevenue; }

        public BigDecimal getTotalDiscount() { return totalDiscount; }
        public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }

        public BigDecimal getTotalCogs() { return totalCogs; }
        public void setTotalCogs(BigDecimal totalCogs) { this.totalCogs = totalCogs; }

        public BigDecimal getTotalNetProfit() { return totalNetProfit; }
        public void setTotalNetProfit(BigDecimal totalNetProfit) { this.totalNetProfit = totalNetProfit; }

        public Double getProfitMargin() { return profitMargin; }
        public void setProfitMargin(Double profitMargin) { this.profitMargin = profitMargin; }

        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

        public Long getPaidOrdersCount() { return paidOrdersCount; }
        public void setPaidOrdersCount(Long paidOrdersCount) { this.paidOrdersCount = paidOrdersCount; }

        public Long getCancelledOrdersCount() { return cancelledOrdersCount; }
        public void setCancelledOrdersCount(Long cancelledOrdersCount) { this.cancelledOrdersCount = cancelledOrdersCount; }

        public Long getVoucherUsedCount() { return voucherUsedCount; }
        public void setVoucherUsedCount(Long voucherUsedCount) { this.voucherUsedCount = voucherUsedCount; }

        public Long getTotalActiveProducts() { return totalActiveProducts; }
        public void setTotalActiveProducts(Long totalActiveProducts) { this.totalActiveProducts = totalActiveProducts; }

        public Long getLowStockCount() { return lowStockCount; }
        public void setLowStockCount(Long lowStockCount) { this.lowStockCount = lowStockCount; }

        public Long getExpiringCount() { return expiringCount; }
        public void setExpiringCount(Long expiringCount) { this.expiringCount = expiringCount; }
    }

    public static class ChartPoint implements Serializable {
        private static final long serialVersionUID = 1L;

        private String label;                                       // Ví dụ: "12/10", "10/2026", "2026"
        private String dateKey;                                     // Khóa chuẩn để sắp xếp (e.g. "2026-10-12")
        private BigDecimal revenue = BigDecimal.ZERO;               // Doanh thu thực tế của kỳ
        private BigDecimal cogs = BigDecimal.ZERO;                  // Giá vốn kỳ
        private BigDecimal profit = BigDecimal.ZERO;                // Lãi thuần kỳ
        private Long orderCount = 0L;                               // Số đơn hàng thành công trong kỳ
        private Long cancelledCount = 0L;                           // Số đơn hủy trong kỳ
        private Long voucherCount = 0L;                             // Số đơn dùng voucher trong kỳ
        private BigDecimal discountAmount = BigDecimal.ZERO;        // Tiền giảm giá voucher trong kỳ

        public ChartPoint() {}

        public ChartPoint(String label, String dateKey) {
            this.label = label;
            this.dateKey = dateKey;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getDateKey() { return dateKey; }
        public void setDateKey(String dateKey) { this.dateKey = dateKey; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public BigDecimal getCogs() { return cogs; }
        public void setCogs(BigDecimal cogs) { this.cogs = cogs; }

        public BigDecimal getProfit() { return profit; }
        public void setProfit(BigDecimal profit) { this.profit = profit; }

        public Long getOrderCount() { return orderCount; }
        public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }

        public Long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(Long cancelledCount) { this.cancelledCount = cancelledCount; }

        public Long getVoucherCount() { return voucherCount; }
        public void setVoucherCount(Long voucherCount) { this.voucherCount = voucherCount; }

        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    }

    public static class ChartData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String period; // "day", "month", "year"
        private List<ChartPoint> points = new ArrayList<>();

        public ChartData() {}

        public ChartData(String period, List<ChartPoint> points) {
            this.period = period;
            this.points = points;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public List<ChartPoint> getPoints() { return points; }
        public void setPoints(List<ChartPoint> points) { this.points = points; }
    }

    public static class InventoryAlertItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private String categoryName;
        private String imageUrl;
        private String unit;
        private BigDecimal price;
        private BigDecimal costPrice;
        private Integer stock;
        private LocalDate expiryDate;
        private Long daysUntilExpiry;                               // Âm nếu đã quá hạn, dương nếu còn hạn
        private String alertType;                                   // "OUT_OF_STOCK", "LOW_STOCK", "EXPIRED", "EXPIRING_SOON"

        public InventoryAlertItem() {}

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public BigDecimal getCostPrice() { return costPrice; }
        public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }

        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

        public Long getDaysUntilExpiry() { return daysUntilExpiry; }
        public void setDaysUntilExpiry(Long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
    }

    public static class InventoryAlertsData implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<InventoryAlertItem> lowStockProducts = new ArrayList<>();
        private List<InventoryAlertItem> expiringProducts = new ArrayList<>();

        public InventoryAlertsData() {}

        public List<InventoryAlertItem> getLowStockProducts() { return lowStockProducts; }
        public void setLowStockProducts(List<InventoryAlertItem> lowStockProducts) { this.lowStockProducts = lowStockProducts; }

        public List<InventoryAlertItem> getExpiringProducts() { return expiringProducts; }
        public void setExpiringProducts(List<InventoryAlertItem> expiringProducts) { this.expiringProducts = expiringProducts; }
    }

    public static class TopProductItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private String categoryName;
        private String imageUrl;
        private String unit;
        private Double soldQuantity;
        private BigDecimal revenue;
        private BigDecimal cogs;
        private BigDecimal profit;

        public TopProductItem() {}

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public Double getSoldQuantity() { return soldQuantity; }
        public void setSoldQuantity(Double soldQuantity) { this.soldQuantity = soldQuantity; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public BigDecimal getCogs() { return cogs; }
        public void setCogs(BigDecimal cogs) { this.cogs = cogs; }

        public BigDecimal getProfit() { return profit; }
        public void setProfit(BigDecimal profit) { this.profit = profit; }
    }
}
