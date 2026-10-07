package com.mycompany.bachhoaxanhonline.module.review;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReviewRepository {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Kiểm tra sản phẩm có tồn tại và chưa bị xóa mềm hay không.
     */
    public boolean existsActiveProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"SanPham\" WHERE \"maSanPham\" = :productId AND \"Deleted\" = FALSE")
                    .setParameter("productId", productId.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra khách hàng đã có đơn hàng thanh toán thành công (DATHANHTOAN) chứa sản phẩm này chưa.
     */
    public boolean hasPaidOrderWithProduct(String customerId, String productId) {
        if (customerId == null || productId == null) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT COUNT(*) FROM \"DonHang\" dh "
                    + "JOIN \"LineItem\" li ON dh.\"maDonHang\" = li.\"maDonHang\" "
                    + "WHERE dh.\"maKhachHang\" = :customerId "
                    + "  AND dh.\"trangThai\" = 'DATHANHTOAN' "
                    + "  AND dh.\"Deleted\" = FALSE "
                    + "  AND li.\"maSanPham\" = :productId "
                    + "  AND li.\"Deleted\" = FALSE";

            Number count = (Number) em.createNativeQuery(sql)
                    .setParameter("customerId", customerId.trim())
                    .setParameter("productId", productId.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra khách hàng đã từng đánh giá sản phẩm này hay chưa.
     */
    public boolean hasReviewed(String customerId, String productId) {
        if (customerId == null || productId == null) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"DanhGia\" WHERE \"maKhachHang\" = :customerId AND \"maSanPham\" = :productId")
                    .setParameter("customerId", customerId.trim())
                    .setParameter("productId", productId.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lưu bài đánh giá mới vào CSDL.
     */
    public Long createReview(Review review) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(review);
            tx.commit();
            return review.getMaDanhGia();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi lưu bài đánh giá: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm bài đánh giá theo ID.
     */
    public Optional<Review> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Review review = em.find(Review.class, id);
            return Optional.ofNullable(review);
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật số sao và nội dung bình luận của bài đánh giá.
     */
    public void updateReview(Long id, int rating, String comment) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Review review = em.find(Review.class, id);
            if (review != null) {
                review.setSoSao(rating);
                review.setNoiDung(comment);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật đánh giá: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Xóa mềm bài đánh giá (Deleted = true).
     */
    public void softDeleteReview(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Review review = em.find(Review.class, id);
            if (review != null) {
                review.setDeleted(true);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi xóa đánh giá: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tóm tắt điểm đánh giá trung bình và số lượng theo từng mức sao.
     */
    @SuppressWarnings("unchecked")
    public ReviewResponse.ReviewSummary getProductReviewSummary(String productId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // 1. Tổng số bài đánh giá đang hiển thị
            Number totalNum = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"DanhGia\" WHERE \"maSanPham\" = :productId AND \"Deleted\" = FALSE")
                    .setParameter("productId", productId)
                    .getSingleResult();
            long total = (totalNum != null) ? totalNum.longValue() : 0L;

            // 2. Điểm trung bình sao
            Number avgNum = (Number) em.createNativeQuery(
                    "SELECT ROUND(AVG(\"soSao\"), 1) FROM \"DanhGia\" WHERE \"maSanPham\" = :productId AND \"Deleted\" = FALSE")
                    .setParameter("productId", productId)
                    .getSingleResult();
            double avg = (avgNum != null) ? avgNum.doubleValue() : 0.0;

            // 3. Phân bố từng mức sao (1..5)
            Map<String, Long> starCounts = new LinkedHashMap<>();
            starCounts.put("5", 0L);
            starCounts.put("4", 0L);
            starCounts.put("3", 0L);
            starCounts.put("2", 0L);
            starCounts.put("1", 0L);

            List<Object[]> starRows = em.createNativeQuery(
                    "SELECT \"soSao\", COUNT(*) FROM \"DanhGia\" WHERE \"maSanPham\" = :productId AND \"Deleted\" = FALSE GROUP BY \"soSao\"")
                    .setParameter("productId", productId)
                    .getResultList();

            for (Object[] r : starRows) {
                if (r[0] != null && r[1] != null) {
                    starCounts.put(r[0].toString(), ((Number) r[1]).longValue());
                }
            }

            return new ReviewResponse.ReviewSummary(avg, total, starCounts);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách đánh giá công khai của 1 sản phẩm.
     */
    @SuppressWarnings("unchecked")
    public List<ReviewResponse.ProductReviewItem> findProductReviews(String productId, Integer rating, int limit, int offset) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT dg.\"maDanhGia\", dg.\"soSao\", dg.\"noiDung\", dg.\"ngayDang\", nd.\"tenND\", nd.\"tenDangNhap\" "
                    + "FROM \"DanhGia\" dg "
                    + "LEFT JOIN \"NguoiDung\" nd ON dg.\"maKhachHang\" = nd.\"maNguoiDung\" "
                    + "WHERE dg.\"maSanPham\" = :productId AND dg.\"Deleted\" = FALSE "
                    + (rating != null ? "AND dg.\"soSao\" = :rating " : "")
                    + "ORDER BY dg.\"ngayDang\" DESC LIMIT :limit OFFSET :offset";

            Query q = em.createNativeQuery(sql)
                    .setParameter("productId", productId)
                    .setParameter("limit", limit)
                    .setParameter("offset", offset);

            if (rating != null) {
                q.setParameter("rating", rating);
            }

            List<Object[]> rows = q.getResultList();
            List<ReviewResponse.ProductReviewItem> items = new ArrayList<>();
            for (Object[] r : rows) {
                Long revId = toLong(r[0]);
                Integer star = toInteger(r[1]);
                String comment = toString(r[2]);
                String created = formatIso8601(r[3]);
                String fullName = toString(r[4]);
                String username = toString(r[5]);

                ReviewResponse.CustomerPublicInfo cust = new ReviewResponse.CustomerPublicInfo(fullName, username);
                items.add(new ReviewResponse.ProductReviewItem(revId, star, comment, created, cust));
            }
            return items;
        } finally {
            em.close();
        }
    }

    /**
     * Đếm tổng số bài đánh giá của chính khách hàng.
     */
    public long countCustomerReviews(String customerId, Integer rating) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT COUNT(*) FROM \"DanhGia\" WHERE \"maKhachHang\" = :customerId AND \"Deleted\" = FALSE "
                    + (rating != null ? "AND \"soSao\" = :rating" : "");
            Query q = em.createNativeQuery(sql).setParameter("customerId", customerId);
            if (rating != null) {
                q.setParameter("rating", rating);
            }
            Number count = (Number) q.getSingleResult();
            return count != null ? count.longValue() : 0L;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách đánh giá của chính khách hàng (kèm thông tin sản phẩm).
     */
    @SuppressWarnings("unchecked")
    public List<ReviewResponse.MyReviewItem> findCustomerReviews(String customerId, Integer rating, int limit, int offset) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT dg.\"maDanhGia\", dg.\"soSao\", dg.\"noiDung\", dg.\"ngayDang\", "
                    + "       sp.\"maSanPham\", sp.\"tenSanPham\", sp.\"hinhAnh\", sp.\"donViTinh\" "
                    + "FROM \"DanhGia\" dg "
                    + "LEFT JOIN \"SanPham\" sp ON dg.\"maSanPham\" = sp.\"maSanPham\" "
                    + "WHERE dg.\"maKhachHang\" = :customerId AND dg.\"Deleted\" = FALSE "
                    + (rating != null ? "AND dg.\"soSao\" = :rating " : "")
                    + "ORDER BY dg.\"ngayDang\" DESC LIMIT :limit OFFSET :offset";

            Query q = em.createNativeQuery(sql)
                    .setParameter("customerId", customerId)
                    .setParameter("limit", limit)
                    .setParameter("offset", offset);

            if (rating != null) {
                q.setParameter("rating", rating);
            }

            List<Object[]> rows = q.getResultList();
            List<ReviewResponse.MyReviewItem> items = new ArrayList<>();
            for (Object[] r : rows) {
                Long revId = toLong(r[0]);
                Integer star = toInteger(r[1]);
                String comment = toString(r[2]);
                String created = formatIso8601(r[3]);
                String spId = toString(r[4]);
                String spName = toString(r[5]);
                String spImage = toString(r[6]);
                String spUnit = toString(r[7]);

                ReviewResponse.ProductDetailInfo prod = new ReviewResponse.ProductDetailInfo(spId, spName, spImage, spUnit);
                items.add(new ReviewResponse.MyReviewItem(revId, star, comment, created, prod));
            }
            return items;
        } finally {
            em.close();
        }
    }

    /**
     * Đếm tổng số bài đánh giá cho Admin / Staff theo các bộ lọc.
     */
    public long countAdminReviews(Boolean isDeleted, String productId, String customerId, Integer rating, String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM \"DanhGia\" dg WHERE 1=1 ");
            appendAdminFilters(sql, isDeleted, productId, customerId, rating, keyword);

            Query q = em.createNativeQuery(sql.toString());
            setAdminParams(q, isDeleted, productId, customerId, rating, keyword);

            Number count = (Number) q.getSingleResult();
            return count != null ? count.longValue() : 0L;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách đánh giá cho Admin / Staff có phân trang và bộ lọc.
     */
    @SuppressWarnings("unchecked")
    public List<ReviewResponse.AdminReviewItem> findAdminReviews(Boolean isDeleted, String productId, String customerId,
                                                                 Integer rating, String keyword, int limit, int offset) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT dg.\"maDanhGia\", dg.\"soSao\", dg.\"noiDung\", dg.\"ngayDang\", dg.\"Deleted\", "
                    + "       nd.\"maNguoiDung\", nd.\"tenND\", nd.\"tenDangNhap\", nd.\"soDienThoai\", "
                    + "       sp.\"maSanPham\", sp.\"tenSanPham\", sp.\"hinhAnh\" "
                    + "FROM \"DanhGia\" dg "
                    + "LEFT JOIN \"NguoiDung\" nd ON dg.\"maKhachHang\" = nd.\"maNguoiDung\" "
                    + "LEFT JOIN \"SanPham\" sp ON dg.\"maSanPham\" = sp.\"maSanPham\" "
                    + "WHERE 1=1 ");

            appendAdminFilters(sql, isDeleted, productId, customerId, rating, keyword);
            sql.append("ORDER BY dg.\"ngayDang\" DESC LIMIT :limit OFFSET :offset");

            Query q = em.createNativeQuery(sql.toString());
            setAdminParams(q, isDeleted, productId, customerId, rating, keyword);
            q.setParameter("limit", limit);
            q.setParameter("offset", offset);

            List<Object[]> rows = q.getResultList();
            List<ReviewResponse.AdminReviewItem> items = new ArrayList<>();
            for (Object[] r : rows) {
                Long revId = toLong(r[0]);
                Integer star = toInteger(r[1]);
                String comment = toString(r[2]);
                String created = formatIso8601(r[3]);
                Boolean deleted = Boolean.TRUE.equals(r[4]);

                String custId = toString(r[5]);
                String fullName = toString(r[6]);
                String username = toString(r[7]);
                String phone = toString(r[8]);
                ReviewResponse.AdminCustomerInfo cust = new ReviewResponse.AdminCustomerInfo(custId, fullName, username, phone);

                String spId = toString(r[9]);
                String spName = toString(r[10]);
                String spImg = toString(r[11]);
                ReviewResponse.AdminProductInfo prod = new ReviewResponse.AdminProductInfo(spId, spName, spImg);

                items.add(new ReviewResponse.AdminReviewItem(revId, star, comment, created, deleted, cust, prod));
            }
            return items;
        } finally {
            em.close();
        }
    }

    private void appendAdminFilters(StringBuilder sql, Boolean isDeleted, String productId, String customerId, Integer rating, String keyword) {
        if (isDeleted != null) {
            sql.append("AND dg.\"Deleted\" = :isDeleted ");
        }
        if (productId != null && !productId.trim().isEmpty()) {
            sql.append("AND dg.\"maSanPham\" = :productId ");
        }
        if (customerId != null && !customerId.trim().isEmpty()) {
            sql.append("AND dg.\"maKhachHang\" = :customerId ");
        }
        if (rating != null) {
            sql.append("AND dg.\"soSao\" = :rating ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND LOWER(COALESCE(dg.\"noiDung\", '')) LIKE :keyword ");
        }
    }

    private void setAdminParams(Query q, Boolean isDeleted, String productId, String customerId, Integer rating, String keyword) {
        if (isDeleted != null) {
            q.setParameter("isDeleted", isDeleted);
        }
        if (productId != null && !productId.trim().isEmpty()) {
            q.setParameter("productId", productId.trim());
        }
        if (customerId != null && !customerId.trim().isEmpty()) {
            q.setParameter("customerId", customerId.trim());
        }
        if (rating != null) {
            q.setParameter("rating", rating);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
    }

    private static String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private static Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.valueOf(obj.toString());
    }

    private static Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.valueOf(obj.toString());
    }

    public static String formatIso8601(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Timestamp) {
            val = ((Timestamp) val).toLocalDateTime();
        }
        if (val instanceof LocalDateTime) {
            return ((LocalDateTime) val).atZone(ZoneOffset.UTC).format(ISO_FORMATTER);
        }
        return val.toString();
    }
}
