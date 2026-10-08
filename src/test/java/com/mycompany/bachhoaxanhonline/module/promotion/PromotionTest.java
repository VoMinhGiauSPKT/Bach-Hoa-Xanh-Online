package com.mycompany.bachhoaxanhonline.module.promotion;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

public class PromotionTest {

    private static final String TEST_CODE_PREFIX = "TEST_KM_";
    private static final String CODE_TIENMAT = TEST_CODE_PREFIX + "TM50K";
    private static final String CODE_PHANTRAM = TEST_CODE_PREFIX + "PT10";

    @BeforeAll
    public static void setUpTestData() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Đảm bảo enum_loai_khuyenmai tồn tại
            em.createNativeQuery(
                    "DO $$ BEGIN "
                    + "  CREATE TYPE enum_loai_khuyenmai AS ENUM ('TIENMAT', 'PHANTRAM'); "
                    + "EXCEPTION WHEN duplicate_object THEN null; "
                    + "END $$;").executeUpdate();

            // Nếu bảng KhuyenMai cũ có các cột khác, recreate lại theo schema chuẩn của user
            boolean hasOldCols = false;
            java.util.List<?> cols = em.createNativeQuery(
                    "SELECT column_name FROM information_schema.columns WHERE LOWER(table_name) = 'khuyenmai' AND column_name = 'loaiGiamGia'").getResultList();
            if (!cols.isEmpty()) {
                hasOldCols = true;
            }

            if (hasOldCols) {
                em.createNativeQuery("DROP TABLE IF EXISTS \"KhuyenMai\" CASCADE").executeUpdate();
            }

            em.createNativeQuery(
                    "CREATE TABLE IF NOT EXISTS \"KhuyenMai\" ("
                    + "\"maKhuyenMai\" VARCHAR(50) PRIMARY KEY, "
                    + "\"tenKhuyenMai\" VARCHAR(255) NOT NULL, "
                    + "\"moTa\" TEXT NULL, "
                    + "\"loaiKhuyenMai\" enum_loai_khuyenmai NOT NULL, "
                    + "\"giaTriGiam\" NUMERIC(12, 2) NOT NULL CHECK (\"giaTriGiam\" > 0), "
                    + "\"giamToiDa\" NUMERIC(12, 2) NULL, "
                    + "\"donHangToiThieu\" NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (\"donHangToiThieu\" >= 0), "
                    + "\"soLuongDung\" INT NOT NULL DEFAULT 100 CHECK (\"soLuongDung\" >= 0), "
                    + "\"ngayBatDau\" TIMESTAMP NOT NULL, "
                    + "\"ngayKetThuc\" TIMESTAMP NOT NULL, "
                    + "\"Deleted\" BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "CONSTRAINT chk_km_phantram CHECK (\"loaiKhuyenMai\" != 'PHANTRAM' OR (\"giaTriGiam\" > 0 AND \"giaTriGiam\" <= 100)), "
                    + "CONSTRAINT chk_km_ngay CHECK (\"ngayKetThuc\" > \"ngayBatDau\")"
                    + ")").executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi thiết lập schema KhuyenMai: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        cleanTestData();
    }




    @BeforeEach
    public void resetPromotions() {
        cleanTestData();

        // Chuẩn bị 2 mã khuyến mãi mẫu đang hoạt động
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Mã tiền mặt: Giảm 50.000đ cho đơn từ 200.000đ
            em.createNativeQuery(
                    "INSERT INTO \"KhuyenMai\" (\"maKhuyenMai\", \"tenKhuyenMai\", \"moTa\", \"loaiKhuyenMai\", "
                    + "\"giaTriGiam\", \"giamToiDa\", \"donHangToiThieu\", \"soLuongDung\", \"ngayBatDau\", \"ngayKetThuc\", \"Deleted\") "
                    + "VALUES (:code, 'Giam 50K don 200K', 'Mo ta test', CAST('TIENMAT' AS enum_loai_khuyenmai), "
                    + "50000, null, 200000, 50, CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP + interval '5 day', false) "
                    + "ON CONFLICT DO NOTHING")
                    .setParameter("code", CODE_TIENMAT)
                    .executeUpdate();

            // 2. Mã phần trăm: Giảm 10% tối đa 30.000đ cho đơn từ 100.000đ
            em.createNativeQuery(
                    "INSERT INTO \"KhuyenMai\" (\"maKhuyenMai\", \"tenKhuyenMai\", \"moTa\", \"loaiKhuyenMai\", "
                    + "\"giaTriGiam\", \"giamToiDa\", \"donHangToiThieu\", \"soLuongDung\", \"ngayBatDau\", \"ngayKetThuc\", \"Deleted\") "
                    + "VALUES (:code, 'Giam 10% toi da 30K', 'Mo ta test', CAST('PHANTRAM' AS enum_loai_khuyenmai), "
                    + "10, 30000, 100000, 30, CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP + interval '5 day', false) "
                    + "ON CONFLICT DO NOTHING")
                    .setParameter("code", CODE_PHANTRAM)
                    .executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi chuẩn bị dữ liệu test: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private static void cleanTestData() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("DELETE FROM \"KhuyenMai\" WHERE \"maKhuyenMai\" LIKE :prefix")
                    .setParameter("prefix", TEST_CODE_PREFIX + "%")
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
        }
    }

    @Test
    public void testGetAvailablePromotions_Calculations() {
        PromotionService service = new PromotionService();

        // 1. totalAmount < 0 -> 400 Bad Request
        PromotionService.PromotionException ex400 = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.getAvailablePromotions(-1000.0)
        );
        Assertions.assertEquals(400, ex400.getStatusCode());

        // 2. Đơn hàng 50.000đ (chưa đạt đơn tối thiểu của cả 2 mã: 100K và 200K)
        PromotionResponse.ApiResponse<List<PromotionResponse.AvailablePromotionItem>> resp50k =
                service.getAvailablePromotions(50000.0);
        Assertions.assertEquals(200, resp50k.getStatus());
        List<PromotionResponse.AvailablePromotionItem> list50k = resp50k.getData();
        Assertions.assertFalse(list50k.isEmpty());
        for (PromotionResponse.AvailablePromotionItem item : list50k) {
            if (item.getPromotionCode().startsWith(TEST_CODE_PREFIX)) {
                Assertions.assertFalse(item.isEligible());
                Assertions.assertNotNull(item.getUnmetReason());
                Assertions.assertEquals(0.0, item.getEstimatedDiscount());
            }
        }

        // 3. Đơn hàng 500.000đ (đủ điều kiện cả 2 mã)
        PromotionResponse.ApiResponse<List<PromotionResponse.AvailablePromotionItem>> resp500k =
                service.getAvailablePromotions(500000.0);
        Assertions.assertEquals(200, resp500k.getStatus());

        for (PromotionResponse.AvailablePromotionItem item : resp500k.getData()) {
            if (CODE_TIENMAT.equals(item.getPromotionCode())) {
                Assertions.assertTrue(item.isEligible());
                Assertions.assertNull(item.getUnmetReason());
                // Tiền mặt: giảm đúng 50.000đ
                Assertions.assertEquals(50000.0, item.getEstimatedDiscount());
            } else if (CODE_PHANTRAM.equals(item.getPromotionCode())) {
                Assertions.assertTrue(item.isEligible());
                Assertions.assertNull(item.getUnmetReason());
                // 10% của 500.000đ = 50.000đ, nhưng trần tối đa là 30.000đ -> phải lấy 30.000đ
                Assertions.assertEquals(30000.0, item.getEstimatedDiscount());
            }
        }
    }

    @Test
    public void testAdminCreatePromotion_ValidationsAndConflict() {
        PromotionService service = new PromotionService();

        // 1. Phân trăm ngoài khoảng [1, 100] -> 400 Bad Request
        PromotionRequest.CreatePromotionRequest reqInvalidPercent = new PromotionRequest.CreatePromotionRequest(
                TEST_CODE_PREFIX + "INV1", "Test", "Desc", "PHANTRAM", 150.0, null, 100000.0, 10,
                "2026-10-01T00:00:00", "2026-10-31T23:59:59"
        );
        PromotionService.PromotionException exPercent = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.createPromotion(reqInvalidPercent)
        );
        Assertions.assertEquals(400, exPercent.getStatusCode());

        // 2. Tiền mặt lớn hơn minOrderAmount -> 400 Bad Request
        PromotionRequest.CreatePromotionRequest reqInvalidCash = new PromotionRequest.CreatePromotionRequest(
                TEST_CODE_PREFIX + "INV2", "Test", "Desc", "TIENMAT", 200000.0, null, 100000.0, 10,
                "2026-10-01T00:00:00", "2026-10-31T23:59:59"
        );
        PromotionService.PromotionException exCash = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.createPromotion(reqInvalidCash)
        );
        Assertions.assertEquals(400, exCash.getStatusCode());

        // 3. Ngày kết thúc trước ngày bắt đầu -> 400 Bad Request
        PromotionRequest.CreatePromotionRequest reqInvalidDate = new PromotionRequest.CreatePromotionRequest(
                TEST_CODE_PREFIX + "INV3", "Test", "Desc", "PHANTRAM", 10.0, 50000.0, 100000.0, 10,
                "2026-10-31T23:59:59", "2026-10-01T00:00:00"
        );
        PromotionService.PromotionException exDate = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.createPromotion(reqInvalidDate)
        );
        Assertions.assertEquals(400, exDate.getStatusCode());

        // 4. Tạo hợp lệ -> 201 Created (tự động chuyển code sang chữ hoa)
        String newCode = TEST_CODE_PREFIX + "valid_new";
        PromotionRequest.CreatePromotionRequest validReq = new PromotionRequest.CreatePromotionRequest(
                newCode, "Khuyen Mai Moi", "Mo ta", "PHANTRAM", 15.0, 50000.0, 200000.0, 100,
                "2026-10-01T00:00:00.000Z", "2026-10-31T23:59:59.000Z"
        );
        PromotionResponse.ApiResponse<PromotionResponse.CreatePromotionData> resp201 =
                service.createPromotion(validReq);
        Assertions.assertEquals(201, resp201.getStatus());
        Assertions.assertEquals(newCode.toUpperCase(), resp201.getData().getPromotionCode());
        Assertions.assertEquals(15.0, resp201.getData().getDiscountValue());

        // 5. Trùng mã -> 409 Conflict
        PromotionService.PromotionException ex409 = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.createPromotion(validReq)
        );
        Assertions.assertEquals(409, ex409.getStatusCode());
    }

    @Test
    public void testAdminGetPromotions_FiltersAndPagination() {
        PromotionService service = new PromotionService();

        // 1. Tìm theo keyword
        PromotionResponse.ApiResponse<PromotionResponse.AdminPromotionsData> respKw =
                service.getAdminPromotions("TM50K", null, 1, 10);
        Assertions.assertEquals(200, respKw.getStatus());
        Assertions.assertTrue(respKw.getData().getTotal() >= 1);
        Assertions.assertTrue(respKw.getData().getPromotions().stream()
                .anyMatch(p -> p.getPromotionCode().equals(CODE_TIENMAT)));

        // 2. Tìm theo loại PHANTRAM
        PromotionResponse.ApiResponse<PromotionResponse.AdminPromotionsData> respType =
                service.getAdminPromotions(null, "PHANTRAM", 1, 10);
        Assertions.assertEquals(200, respType.getStatus());
        Assertions.assertTrue(respType.getData().getPromotions().stream()
                .anyMatch(p -> p.getPromotionCode().equals(CODE_PHANTRAM)));

        // 3. Trạng thái mã đang hoạt động -> ACTIVE
        PromotionResponse.AdminPromotionItem item = respKw.getData().getPromotions().stream()
                .filter(p -> p.getPromotionCode().equals(CODE_TIENMAT))
                .findFirst().orElse(null);
        Assertions.assertNotNull(item);
        Assertions.assertEquals("ACTIVE", item.getStatus());
    }

    @Test
    public void testAdminUpdatePromotion() {
        PromotionService service = new PromotionService();

        // 1. Cập nhật mã không tồn tại -> 404
        PromotionRequest.UpdatePromotionRequest updateReq = new PromotionRequest.UpdatePromotionRequest(
                "Ten Moi", "Mo ta moi", 80, "2026-12-31T23:59:59"
        );
        PromotionService.PromotionException ex404 = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.updatePromotion("MA_KHONG_CO_9999", updateReq)
        );
        Assertions.assertEquals(404, ex404.getStatusCode());

        // 2. Cập nhật chính xác -> 200 OK
        PromotionResponse.ApiResponse<PromotionResponse.UpdatePromotionData> resp200 =
                service.updatePromotion(CODE_TIENMAT, updateReq);
        Assertions.assertEquals(200, resp200.getStatus());
        Assertions.assertEquals(CODE_TIENMAT, resp200.getData().getPromotionCode());
        Assertions.assertEquals("Ten Moi", resp200.getData().getPromotionName());
        Assertions.assertEquals(80, resp200.getData().getRemainingUsage());
    }

    @Test
    public void testAdminDeletePromotion() {
        PromotionService service = new PromotionService();

        // 1. Xóa mã không tồn tại -> 404
        PromotionService.PromotionException ex404 = Assertions.assertThrows(
                PromotionService.PromotionException.class,
                () -> service.deletePromotion("MA_KHONG_CO_9999")
        );
        Assertions.assertEquals(404, ex404.getStatusCode());

        // 2. Xóa mã hợp lệ -> 200 OK
        PromotionResponse.ApiResponse<Void> resp200 = service.deletePromotion(CODE_PHANTRAM);
        Assertions.assertEquals(200, resp200.getStatus());

        // 3. Kiểm tra mã đã thực sự bị xóa khỏi database
        PromotionRepository repo = new PromotionRepository();
        Assertions.assertFalse(repo.existsByCode(CODE_PHANTRAM));
    }
}
