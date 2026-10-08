package com.mycompany.bachhoaxanhonline.module.review;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class ReviewTest {

    private static String testProductId;
    private static final String TEST_CUSTOMER_ID = "KH_TEST_" + (System.currentTimeMillis() % 100000);
    private static final String OTHER_CUSTOMER_ID = "KH_OTHER_" + (System.currentTimeMillis() % 100000);

    @BeforeAll
    public static void setUpTestData() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Tạo người dùng khách hàng test với số điện thoại độc nhất
            String phone1 = "09" + String.format("%08d", Math.abs(TEST_CUSTOMER_ID.hashCode()) % 100000000);
            String phone2 = "08" + String.format("%08d", Math.abs(OTHER_CUSTOMER_ID.hashCode()) % 100000000);

            em.createNativeQuery(
                    "INSERT INTO \"NguoiDung\" (\"maNguoiDung\", \"tenND\", \"tenDangNhap\", \"soDienThoai\", \"matKhauHashed\", \"Deleted\") "
                    + "VALUES (:id, 'Khach Hang Test', :user, :phone, 'hashed', false) ON CONFLICT DO NOTHING")
                    .setParameter("id", TEST_CUSTOMER_ID)
                    .setParameter("user", "user_" + TEST_CUSTOMER_ID)
                    .setParameter("phone", phone1)
                    .executeUpdate();

            em.createNativeQuery(
                    "INSERT INTO \"KhachHang\" (\"maKhachHang\") VALUES (:id) ON CONFLICT DO NOTHING")
                    .setParameter("id", TEST_CUSTOMER_ID)
                    .executeUpdate();

            em.createNativeQuery(
                    "INSERT INTO \"NguoiDung\" (\"maNguoiDung\", \"tenND\", \"tenDangNhap\", \"soDienThoai\", \"matKhauHashed\", \"Deleted\") "
                    + "VALUES (:id, 'Nguoi Dung Khac', :user, :phone, 'hashed', false) ON CONFLICT DO NOTHING")
                    .setParameter("id", OTHER_CUSTOMER_ID)
                    .setParameter("user", "user_" + OTHER_CUSTOMER_ID)
                    .setParameter("phone", phone2)
                    .executeUpdate();


            em.createNativeQuery(
                    "INSERT INTO \"KhachHang\" (\"maKhachHang\") VALUES (:id) ON CONFLICT DO NOTHING")
                    .setParameter("id", OTHER_CUSTOMER_ID)
                    .executeUpdate();

            // Tạo Loại sản phẩm test và Nhà cung cấp test nếu cần
            String maLoaiTest = "LOAI_TEST";
            String maNccTest = "NCC_TEST";
            em.createNativeQuery(
                    "INSERT INTO \"LoaiSanPham\" (\"maLoaiSanPham\", \"tenLoaiSanPham\", \"phanTramLoiNhuan\", \"Deleted\") "
                    + "VALUES ('LOAI_TEST', 'Thuc Pham', 0.1, false) ON CONFLICT DO NOTHING")
                    .executeUpdate();

            em.createNativeQuery(
                    "INSERT INTO \"NhaCungCap\" (\"maNhaCungCap\", \"tenNhaCungCap\", \"dangHopTac\", \"Deleted\") "
                    + "VALUES ('NCC_TEST', 'Vinamilk', true, false) ON CONFLICT DO NOTHING")
                    .executeUpdate();

            java.util.List<?> existingProds = em.createNativeQuery("SELECT \"maSanPham\" FROM \"SanPham\" LIMIT 1").getResultList();
            if (!existingProds.isEmpty()) {
                testProductId = existingProds.get(0).toString();
            } else {
                testProductId = "SP_TEST_" + (System.currentTimeMillis() % 100000);
                em.createNativeQuery(
                        "INSERT INTO \"SanPham\" (\"maSanPham\", \"tenSanPham\", \"donViTinh\", \"giaBan\", \"giaNhap\", \"soLuong\", \"phiVAT\", \"maLoai\", \"maNhaCungCap\", \"Deleted\") "
                        + "VALUES (:spId, 'Sua Tuoi Vinamilk', 'CHAI', 25000, 20000, 100, 0.08, 'LOAI_TEST', 'NCC_TEST', false) ON CONFLICT DO NOTHING")
                        .setParameter("spId", testProductId)
                        .executeUpdate();
            }

            // 3. Tạo đơn hàng DATHANHTOAN cho TEST_CUSTOMER_ID mua testProductId
            String maDonHang = "DH_TEST_" + (System.currentTimeMillis() % 100000);
            em.createNativeQuery(
                    "INSERT INTO \"DonHang\" (\"maDonHang\", \"maKhachHang\", \"trangThai\", \"Deleted\", \"ngayLap\", \"ngayHetHanThanhToan\", \"tongTien\", \"tongTienSauGiamGia\", \"tienGiamGia\") "
                    + "VALUES (:dhId, :khId, 'DATHANHTOAN', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '1 day', 50000, 50000, 0) ON CONFLICT DO NOTHING")
                    .setParameter("dhId", maDonHang)
                    .setParameter("khId", TEST_CUSTOMER_ID)
                    .executeUpdate();

            em.createNativeQuery(
                    "INSERT INTO \"LineItem\" (\"maDonHang\", \"maSanPham\", \"soLuong\", \"thanhTien\", \"Deleted\") "
                    + "VALUES (:dhId, :spId, 2, 50000, false)")
                    .setParameter("dhId", maDonHang)
                    .setParameter("spId", testProductId)
                    .executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi chuẩn bị dữ liệu test: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @BeforeEach
    public void cleanReviews() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("DELETE FROM \"DanhGia\" WHERE \"maKhachHang\" IN (:c1, :c2)")
                    .setParameter("c1", TEST_CUSTOMER_ID)
                    .setParameter("c2", OTHER_CUSTOMER_ID)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
        }
    }


    @Test
    public void testGetProductReviews_PublicAndNotFound() {
        ReviewService service = new ReviewService();

        // 1. Sản phẩm không tồn tại -> 404
        ReviewService.ReviewException ex404 = Assertions.assertThrows(
                ReviewService.ReviewException.class,
                () -> service.getProductReviews("SP_KHONG_TON_TAI_9999", null, 1, 5)
        );
        Assertions.assertEquals(404, ex404.getStatusCode());

        // 2. Sản phẩm tồn tại hợp lệ -> 200 OK
        ReviewResponse.ApiResponse<ReviewResponse.ProductReviewsData> resp =
                service.getProductReviews(testProductId, null, 1, 5);
        Assertions.assertEquals(200, resp.getStatus());
        Assertions.assertNotNull(resp.getData());
        Assertions.assertNotNull(resp.getData().getSummary());
    }

    @Test
    public void testCreateReview_FlowValidationAndConflict() {
        ReviewService service = new ReviewService();

        // 1. Khách hàng OTHER_CUSTOMER chưa mua sản phẩm -> 403 Forbidden
        ReviewRequest.CreateReviewRequest reqOther = new ReviewRequest.CreateReviewRequest(
                testProductId, 5, "Chua mua ma danh gia");
        ReviewService.ReviewException ex403 = Assertions.assertThrows(
                ReviewService.ReviewException.class,
                () -> service.createReview(OTHER_CUSTOMER_ID, reqOther)
        );
        Assertions.assertEquals(403, ex403.getStatusCode());

        // 2. Số sao ngoài khoảng [1, 5] -> 400 Bad Request
        ReviewRequest.CreateReviewRequest reqInvalidStar = new ReviewRequest.CreateReviewRequest(
                testProductId, 6, "So sao sai");
        ReviewService.ReviewException ex400 = Assertions.assertThrows(
                ReviewService.ReviewException.class,
                () -> service.createReview(TEST_CUSTOMER_ID, reqInvalidStar)
        );
        Assertions.assertEquals(400, ex400.getStatusCode());

        // 3. Khách hàng TEST_CUSTOMER đã mua -> 201 Created thành công
        ReviewRequest.CreateReviewRequest validReq = new ReviewRequest.CreateReviewRequest(
                testProductId, 5, "Sua uong rat ngon va dam da!");
        ReviewResponse.ApiResponse<ReviewResponse.CreateReviewData> resp201 =
                service.createReview(TEST_CUSTOMER_ID, validReq);
        Assertions.assertEquals(201, resp201.getStatus());
        Assertions.assertNotNull(resp201.getData().getReviewId());
        Assertions.assertEquals(5, resp201.getData().getRating());
        Assertions.assertEquals(testProductId, resp201.getData().getProductId());

        // 4. Khách hàng này cố tình đánh giá lần 2 trên cùng sản phẩm -> 409 Conflict
        ReviewService.ReviewException ex409 = Assertions.assertThrows(
                ReviewService.ReviewException.class,
                () -> service.createReview(TEST_CUSTOMER_ID, validReq)
        );
        Assertions.assertEquals(409, ex409.getStatusCode());
    }

    @Test
    public void testUpdateReview_OwnershipAndSuccess() {
        ReviewService service = new ReviewService();
        ReviewRepository repo = new ReviewRepository();

        // Tạo trực tiếp 1 review để test cập nhật
        Review r = new Review(TEST_CUSTOMER_ID, testProductId, 3, "Binh thuong", LocalDateTime.now());
        Long reviewId = repo.createReview(r);

        // 1. Khách hàng khác cố tình cập nhật -> 403 Forbidden
        ReviewRequest.UpdateReviewRequest updateReq = new ReviewRequest.UpdateReviewRequest(4, "Sua tam duoc");
        ReviewService.ReviewException ex403 = Assertions.assertThrows(
                ReviewService.ReviewException.class,
                () -> service.updateReview(OTHER_CUSTOMER_ID, reviewId, updateReq)
        );
        Assertions.assertEquals(403, ex403.getStatusCode());

        // 2. Chính chủ cập nhật -> 200 OK
        ReviewResponse.ApiResponse<ReviewResponse.UpdateReviewData> resp200 =
                service.updateReview(TEST_CUSTOMER_ID, reviewId, updateReq);
        Assertions.assertEquals(200, resp200.getStatus());
        Assertions.assertEquals(4, resp200.getData().getRating());
        Assertions.assertEquals("Sua tam duoc", resp200.getData().getComment());
    }

    @Test
    public void testDeleteReview_RolePermissions() {
        ReviewService service = new ReviewService();
        ReviewRepository repo = new ReviewRepository();

        // 1. Tạo review thuộc sở hữu của TEST_CUSTOMER_ID
        Review r1 = new Review(TEST_CUSTOMER_ID, testProductId, 4, "Danh gia de xoa 1", LocalDateTime.now());
        Long id1 = repo.createReview(r1);

        // Khách hàng khác xóa bài của TEST_CUSTOMER -> 403 Forbidden
        ReviewService.ReviewException ex403 = Assertions.assertThrows(
                ReviewService.ReviewException.class,
                () -> service.deleteReview(OTHER_CUSTOMER_ID, "CUSTOMER", id1)
        );
        Assertions.assertEquals(403, ex403.getStatusCode());

        // Chính chủ xóa -> 200 OK thành công
        ReviewResponse.ApiResponse<Void> delCust = service.deleteReview(TEST_CUSTOMER_ID, "CUSTOMER", id1);
        Assertions.assertEquals(200, delCust.getStatus());

        // 2. Tạo review khác để STAFF / ADMIN xóa
        Review r2 = new Review(OTHER_CUSTOMER_ID, testProductId, 2, "Danh gia spam de xoa", LocalDateTime.now());
        Long id2 = repo.createReview(r2);


        // STAFF xóa bài của bất kỳ khách hàng nào -> 200 OK thành công
        ReviewResponse.ApiResponse<Void> delStaff = service.deleteReview("STAFF_001", "STAFF", id2);
        Assertions.assertEquals(200, delStaff.getStatus());
    }

    @Test
    public void testGetMyReviews_AndAdminReviews() {
        ReviewService service = new ReviewService();

        // 1. Lấy danh sách review của chính tôi
        ReviewResponse.ApiResponse<ReviewResponse.MyReviewsData> myResp =
                service.getMyReviews(TEST_CUSTOMER_ID, null, 1, 10);
        Assertions.assertEquals(200, myResp.getStatus());
        Assertions.assertNotNull(myResp.getData());
        Assertions.assertTrue(myResp.getData().getTotal() >= 0);

        // 2. Lấy danh sách review cho Quản trị viên / Nhân viên
        ReviewResponse.ApiResponse<ReviewResponse.AdminReviewsData> adminResp =
                service.getAdminReviews(false, null, null, null, null, 1, 20);
        Assertions.assertEquals(200, adminResp.getStatus());
        Assertions.assertNotNull(adminResp.getData());
        Assertions.assertTrue(adminResp.getData().getTotal() >= 0);
    }
}
