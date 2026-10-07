package com.mycompany.bachhoaxanhonline.module.promotion;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PromotionService {

    private final PromotionRepository promotionRepository = new PromotionRepository();

    public static class PromotionException extends RuntimeException {
        private final int statusCode;

        public PromotionException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Trường hợp ISO có Z hoặc offset: 2026-10-06T00:00:00.000Z
            if (dateStr.endsWith("Z") || dateStr.contains("+")) {
                Instant instant = Instant.parse(dateStr);
                return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
            }
            // Trường hợp LocalDateTime thông thường
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                // Thử định dạng yyyy-MM-dd
                return LocalDateTime.parse(dateStr + "T00:00:00");
            } catch (Exception ex) {
                throw new PromotionException(400, "Định dạng ngày không hợp lệ: " + dateStr);
            }
        }
    }

    /**
     * 1. Lấy danh sách khuyến mãi khả dụng (GET /promotion/available)
     */
    public PromotionResponse.ApiResponse<List<PromotionResponse.AvailablePromotionItem>> getAvailablePromotions(Double totalAmount) {
        if (totalAmount == null || totalAmount < 0) {
            throw new PromotionException(400, "totalAmount không hợp lệ.");
        }

        List<PromotionResponse.AvailablePromotionItem> list = promotionRepository.getAvailablePromotions(totalAmount);
        return new PromotionResponse.ApiResponse<>(200, "Lấy danh sách khuyến mãi thành công", list);
    }

    /**
     * 2. Admin xem toàn bộ danh sách khuyến mãi (GET /promotion)
     */
    public PromotionResponse.ApiResponse<PromotionResponse.AdminPromotionsData> getAdminPromotions(
            String keyword, String type, Integer page, Integer limit) {

        int currentPage = (page != null && page >= 1) ? page : 1;
        int currentLimit = (limit != null && limit >= 1) ? limit : 10;

        if (type != null && !type.trim().isEmpty()) {
            String upperType = type.trim().toUpperCase();
            if (!"TIENMAT".equals(upperType) && !"PHANTRAM".equals(upperType)) {
                throw new PromotionException(400, "Loại khuyến mãi không hợp lệ: phải là TIENMAT hoặc PHANTRAM.");
            }
            type = upperType;
        }

        PromotionResponse.AdminPromotionsData data = promotionRepository.getAdminPromotions(keyword, type, currentPage, currentLimit);
        return new PromotionResponse.ApiResponse<>(200, "Lấy danh sách khuyến mãi quản trị thành công", data);
    }

    /**
     * 3. Admin tạo mới khuyến mãi (POST /promotion)
     */
    public PromotionResponse.ApiResponse<PromotionResponse.CreatePromotionData> createPromotion(PromotionRequest.CreatePromotionRequest req) {
        if (req == null) {
            throw new PromotionException(400, "Dữ liệu không hợp lệ.");
        }

        if (req.getPromotionCode() == null || req.getPromotionCode().trim().isEmpty()) {
            throw new PromotionException(400, "Mã khuyến mãi không được để trống.");
        }
        if (req.getPromotionName() == null || req.getPromotionName().trim().isEmpty()) {
            throw new PromotionException(400, "Tên khuyến mãi không được để trống.");
        }
        if (req.getDiscountType() == null || req.getDiscountType().trim().isEmpty()) {
            throw new PromotionException(400, "Loại khuyến mãi không được để trống.");
        }
        if (req.getDiscountValue() == null || req.getDiscountValue() <= 0) {
            throw new PromotionException(400, "Giá trị giảm phải lớn hơn 0.");
        }

        // Tự động chuyển promotionCode sang chữ in hoa không dấu
        String code = req.getPromotionCode().trim().toUpperCase();
        req.setPromotionCode(code);

        String type = req.getDiscountType().trim().toUpperCase();
        req.setDiscountType(type);

        if (!"TIENMAT".equals(type) && !"PHANTRAM".equals(type)) {
            throw new PromotionException(400, "Loại khuyến mãi chỉ chấp nhận TIENMAT hoặc PHANTRAM.");
        }

        // Quy tắc: discountType là PHANTRAM thì discountValue phải từ 1 đến 100%
        if ("PHANTRAM".equals(type)) {
            if (req.getDiscountValue() < 1 || req.getDiscountValue() > 100) {
                throw new PromotionException(400, "discountType là PHANTRAM thì discountValue phải từ 1 đến 100%.");
            }
        }

        // Quy tắc: discountType là TIENMAT thì discountValue phải <= minOrderAmount
        double minOrder = req.getMinOrderAmount() != null ? req.getMinOrderAmount() : 0.0;
        if ("TIENMAT".equals(type)) {
            if (req.getDiscountValue() > minOrder) {
                throw new PromotionException(400, "discountType là TIENMAT thì discountValue phải <= minOrderAmount.");
            }
        }

        // Kiểm tra ngày bắt đầu & ngày kết thúc
        LocalDateTime startLdt = parseDateTime(req.getStartDate());
        LocalDateTime endLdt = parseDateTime(req.getEndDate());
        if (startLdt == null || endLdt == null) {
            throw new PromotionException(400, "startDate và endDate không được để trống.");
        }
        if (!endLdt.isAfter(startLdt)) {
            throw new PromotionException(400, "ngày kết thúc phải sau ngày bắt đầu.");
        }

        // Kiểm tra trùng mã
        if (promotionRepository.existsByCode(code)) {
            throw new PromotionException(409, "Mã khuyến mãi (" + code + ") đã tồn tại trong hệ thống.");
        }

        int usage = req.getUsageLimit() != null ? req.getUsageLimit() : 100;
        if (usage < 0) {
            throw new PromotionException(400, "usageLimit không được âm.");
        }

        promotionRepository.createPromotion(req, startLdt, endLdt);

        PromotionResponse.CreatePromotionData data = new PromotionResponse.CreatePromotionData(
                code, req.getPromotionName(), type, req.getDiscountValue(), req.getMaxDiscount(), usage
        );
        return new PromotionResponse.ApiResponse<>(201, "Tạo chương trình khuyến mãi thành công", data);
    }

    /**
     * 4. Admin cập nhật khuyến mãi (PUT /promotion/:code)
     */
    public PromotionResponse.ApiResponse<PromotionResponse.UpdatePromotionData> updatePromotion(
            String code, PromotionRequest.UpdatePromotionRequest req) {

        if (code == null || code.trim().isEmpty()) {
            throw new PromotionException(400, "Mã khuyến mãi không hợp lệ.");
        }
        code = code.trim().toUpperCase();

        Promotion existing = promotionRepository.findByCode(code);
        if (existing == null) {
            throw new PromotionException(404, "Mã khuyến mãi không tồn tại.");
        }

        LocalDateTime endLdt = null;
        if (req != null && req.getEndDate() != null && !req.getEndDate().trim().isEmpty()) {
            endLdt = parseDateTime(req.getEndDate());
            if (!endLdt.isAfter(existing.getNgayBatDau())) {
                throw new PromotionException(400, "ngày kết thúc phải sau ngày bắt đầu.");
            }
        }

        String name = (req != null && req.getPromotionName() != null) ? req.getPromotionName().trim() : null;
        String desc = (req != null && req.getDescription() != null) ? req.getDescription().trim() : null;
        Integer usage = (req != null) ? req.getRemainingUsage() : null;
        if (usage != null && usage < 0) {
            throw new PromotionException(400, "remainingUsage không được âm.");
        }

        promotionRepository.updatePromotion(code, name, desc, usage, endLdt);

        String finalName = name != null ? name : existing.getTenKhuyenMai();
        int finalUsage = usage != null ? usage : existing.getSoLuongDung();
        String finalEndDate = req != null && req.getEndDate() != null ? req.getEndDate() : existing.getNgayKetThuc().toString();

        PromotionResponse.UpdatePromotionData data = new PromotionResponse.UpdatePromotionData(
                code, finalName, finalUsage, finalEndDate
        );
        return new PromotionResponse.ApiResponse<>(200, "Cập nhật khuyến mãi thành công", data);
    }

    /**
     * 5. Admin xóa khuyến mãi (DELETE /promotion/:code)
     */
    public PromotionResponse.ApiResponse<Void> deletePromotion(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new PromotionException(400, "Mã khuyến mãi không hợp lệ.");
        }
        code = code.trim().toUpperCase();

        if (!promotionRepository.existsByCode(code)) {
            throw new PromotionException(404, "Mã khuyến mãi không tồn tại.");
        }

        promotionRepository.deletePromotion(code);
        return new PromotionResponse.ApiResponse<>(200, "Xóa mã khuyến mãi thành công");
    }
}
