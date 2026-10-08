package com.mycompany.bachhoaxanhonline.module.promotion;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PromotionRepository {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String formatIsoDateTime(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        return ldt.atZone(ZoneId.of("UTC")).format(ISO_FORMATTER);
    }

    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        return null;
    }

    /**
     * 1. Lấy danh sách khuyến mãi khả dụng (GET /promotion/available)
     */
    public List<PromotionResponse.AvailablePromotionItem> getAvailablePromotions(double totalAmount) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT \"maKhuyenMai\", \"tenKhuyenMai\", \"moTa\", CAST(\"loaiKhuyenMai\" AS varchar), "
                    + "\"giaTriGiam\", \"giamToiDa\", \"donHangToiThieu\", \"soLuongDung\", "
                    + "\"ngayBatDau\", \"ngayKetThuc\" "
                    + "FROM \"KhuyenMai\" "
                    + "WHERE \"Deleted\" = FALSE "
                    + "  AND \"soLuongDung\" > 0 "
                    + "  AND \"ngayBatDau\" <= CURRENT_TIMESTAMP "
                    + "  AND \"ngayKetThuc\" >= CURRENT_TIMESTAMP "
                    + "ORDER BY \"ngayBatDau\" DESC";

            Query query = em.createNativeQuery(sql);
            List<?> rows = query.getResultList();

            List<PromotionResponse.AvailablePromotionItem> result = new ArrayList<>();
            for (Object obj : rows) {
                Object[] row = (Object[]) obj;
                String code = (String) row[0];
                String name = (String) row[1];
                String desc = (String) row[2];
                String discountType = (String) row[3];
                double discountValue = ((Number) row[4]).doubleValue();
                Double maxDiscount = row[5] != null ? ((Number) row[5]).doubleValue() : null;
                double minOrderAmount = ((Number) row[6]).doubleValue();

                // Logic kiểm tra điều kiện áp dụng & tính tiền giảm dự tính
                boolean isEligible = totalAmount >= minOrderAmount;
                String unmetReason = isEligible ? null : String.format("Đơn hàng tối thiểu phải từ %,.0fđ trở lên", minOrderAmount);

                double estimatedDiscount = 0.0;
                if (isEligible) {
                    if ("TIENMAT".equalsIgnoreCase(discountType)) {
                        estimatedDiscount = discountValue;
                    } else if ("PHANTRAM".equalsIgnoreCase(discountType)) {
                        estimatedDiscount = totalAmount * (discountValue / 100.0);
                        if (maxDiscount != null && estimatedDiscount > maxDiscount) {
                            estimatedDiscount = maxDiscount;
                        }
                    }
                }

                result.add(new PromotionResponse.AvailablePromotionItem(
                        code, name, desc, discountType, discountValue,
                        minOrderAmount, maxDiscount, estimatedDiscount,
                        isEligible, unmetReason
                ));
            }
            return result;
        } finally {
            em.close();
        }
    }

    /**
     * 2. Admin xem toàn bộ danh sách khuyến mãi (GET /promotion)
     */
    public PromotionResponse.AdminPromotionsData getAdminPromotions(String keyword, String type, int page, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder whereSql = new StringBuilder(" WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                whereSql.append(" AND (LOWER(\"maKhuyenMai\") LIKE :keyword OR LOWER(\"tenKhuyenMai\") LIKE :keyword)");
            }
            if (type != null && !type.trim().isEmpty()) {
                whereSql.append(" AND CAST(\"loaiKhuyenMai\" AS varchar) = :type");
            }

            // Đếm tổng số
            String countSql = "SELECT COUNT(1) FROM \"KhuyenMai\"" + whereSql;
            Query countQuery = em.createNativeQuery(countSql);
            if (keyword != null && !keyword.trim().isEmpty()) {
                countQuery.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (type != null && !type.trim().isEmpty()) {
                countQuery.setParameter("type", type.trim().toUpperCase());
            }
            long total = ((Number) countQuery.getSingleResult()).longValue();

            // Truy vấn danh sách
            String selectSql = "SELECT \"maKhuyenMai\", \"tenKhuyenMai\", CAST(\"loaiKhuyenMai\" AS varchar), "
                    + "\"giaTriGiam\", \"giamToiDa\", \"donHangToiThieu\", \"soLuongDung\", "
                    + "\"ngayBatDau\", \"ngayKetThuc\", \"Deleted\" "
                    + "FROM \"KhuyenMai\""
                    + whereSql
                    + " ORDER BY \"ngayBatDau\" DESC "
                    + "LIMIT :limit OFFSET :offset";

            Query listQuery = em.createNativeQuery(selectSql);
            if (keyword != null && !keyword.trim().isEmpty()) {
                listQuery.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (type != null && !type.trim().isEmpty()) {
                listQuery.setParameter("type", type.trim().toUpperCase());
            }

            int offset = (page - 1) * limit;
            listQuery.setParameter("limit", limit);
            listQuery.setParameter("offset", offset);

            List<?> rows = listQuery.getResultList();
            List<PromotionResponse.AdminPromotionItem> promotions = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Object obj : rows) {
                Object[] row = (Object[]) obj;
                String code = (String) row[0];
                String name = (String) row[1];
                String discountType = (String) row[2];
                double discountValue = ((Number) row[3]).doubleValue();
                Double maxDiscount = row[4] != null ? ((Number) row[4]).doubleValue() : null;
                double minOrderAmount = ((Number) row[5]).doubleValue();
                int remainingUsage = ((Number) row[6]).intValue();
                LocalDateTime startLdt = toLocalDateTime(row[7]);
                LocalDateTime endLdt = toLocalDateTime(row[8]);
                boolean isDeleted = Boolean.TRUE.equals(row[9]);

                // Tính toán trạng thái status
                String status;
                if (isDeleted) {
                    status = "DELETED";
                } else if (remainingUsage <= 0) {
                    status = "OUT_OF_STOCK";
                } else if (now.isAfter(endLdt)) {
                    status = "EXPIRED";
                } else if (now.isBefore(startLdt)) {
                    status = "UPCOMING";
                } else {
                    status = "ACTIVE";
                }

                promotions.add(new PromotionResponse.AdminPromotionItem(
                        code, name, discountType, discountValue, maxDiscount,
                        minOrderAmount, remainingUsage,
                        formatIsoDateTime(startLdt), formatIsoDateTime(endLdt),
                        isDeleted, status
                ));
            }

            return new PromotionResponse.AdminPromotionsData(total, page, limit, promotions);
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra mã khuyến mãi đã tồn tại chưa
     */
    public boolean existsByCode(String code) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createNativeQuery(
                    "SELECT COUNT(1) FROM \"KhuyenMai\" WHERE \"maKhuyenMai\" = :code");
            query.setParameter("code", code);
            return ((Number) query.getSingleResult()).longValue() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy thông tin khuyến mãi theo mã
     */
    public Promotion findByCode(String code) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT \"maKhuyenMai\", \"tenKhuyenMai\", \"moTa\", CAST(\"loaiKhuyenMai\" AS varchar), "
                    + "\"giaTriGiam\", \"giamToiDa\", \"donHangToiThieu\", \"soLuongDung\", "
                    + "\"ngayBatDau\", \"ngayKetThuc\", \"Deleted\" "
                    + "FROM \"KhuyenMai\" WHERE \"maKhuyenMai\" = :code";

            Query query = em.createNativeQuery(sql);
            query.setParameter("code", code);
            List<?> rows = query.getResultList();
            if (rows.isEmpty()) {
                return null;
            }

            Object[] row = (Object[]) rows.get(0);
            Promotion p = new Promotion();
            p.setMaKhuyenMai((String) row[0]);
            p.setTenKhuyenMai((String) row[1]);
            p.setMoTa((String) row[2]);
            p.setLoaiKhuyenMai((String) row[3]);
            p.setGiaTriGiam(row[4] != null ? BigDecimal.valueOf(((Number) row[4]).doubleValue()) : null);
            p.setGiamToiDa(row[5] != null ? BigDecimal.valueOf(((Number) row[5]).doubleValue()) : null);
            p.setDonHangToiThieu(row[6] != null ? BigDecimal.valueOf(((Number) row[6]).doubleValue()) : null);
            p.setSoLuongDung(row[7] != null ? ((Number) row[7]).intValue() : 0);
            p.setNgayBatDau(toLocalDateTime(row[8]));
            p.setNgayKetThuc(toLocalDateTime(row[9]));
            p.setDeleted(Boolean.TRUE.equals(row[10]));
            return p;
        } finally {
            em.close();
        }
    }

    /**
     * 3. Admin tạo mới khuyến mãi (POST /promotion)
     */
    public void createPromotion(PromotionRequest.CreatePromotionRequest req, LocalDateTime startLdt, LocalDateTime endLdt) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String sql = "INSERT INTO \"KhuyenMai\" ("
                    + "\"maKhuyenMai\", \"tenKhuyenMai\", \"moTa\", \"loaiKhuyenMai\", "
                    + "\"giaTriGiam\", \"giamToiDa\", \"donHangToiThieu\", \"soLuongDung\", "
                    + "\"ngayBatDau\", \"ngayKetThuc\", \"Deleted\") "
                    + "VALUES (:code, :name, :desc, CAST(:type AS enum_loai_khuyenmai), "
                    + ":val, :maxVal, :minOrder, :usage, :start, :end, false)";

            Query query = em.createNativeQuery(sql);
            query.setParameter("code", req.getPromotionCode());
            query.setParameter("name", req.getPromotionName());
            query.setParameter("desc", req.getDescription());
            query.setParameter("type", req.getDiscountType().trim().toUpperCase());
            query.setParameter("val", req.getDiscountValue());
            query.setParameter("maxVal", req.getMaxDiscount());
            query.setParameter("minOrder", req.getMinOrderAmount() != null ? req.getMinOrderAmount() : 0.0);
            query.setParameter("usage", req.getUsageLimit() != null ? req.getUsageLimit() : 100);
            query.setParameter("start", startLdt);
            query.setParameter("end", endLdt);

            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi lưu khuyến mãi: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * 4. Admin cập nhật khuyến mãi (PUT /promotion/:code)
     */
    public boolean updatePromotion(String code, String name, String desc, Integer remainingUsage, LocalDateTime endLdt) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String sql = "UPDATE \"KhuyenMai\" SET "
                    + "\"tenKhuyenMai\" = COALESCE(:name, \"tenKhuyenMai\"), "
                    + "\"moTa\" = COALESCE(:desc, \"moTa\"), "
                    + "\"soLuongDung\" = COALESCE(:usage, \"soLuongDung\"), "
                    + "\"ngayKetThuc\" = COALESCE(:endDate, \"ngayKetThuc\") "
                    + "WHERE \"maKhuyenMai\" = :code";

            Query query = em.createNativeQuery(sql);
            query.setParameter("code", code);
            query.setParameter("name", name);
            query.setParameter("desc", desc);
            query.setParameter("usage", remainingUsage);
            query.setParameter("endDate", endLdt);

            int updated = query.executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật khuyến mãi: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * 5. Admin xóa khuyến mãi (DELETE /promotion/:code - Xóa cứng)
     */
    public boolean deletePromotion(String code) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query query = em.createNativeQuery(
                    "DELETE FROM \"KhuyenMai\" WHERE \"maKhuyenMai\" = :code");
            query.setParameter("code", code);
            int deleted = query.executeUpdate();
            tx.commit();
            return deleted > 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi xóa khuyến mãi: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
