package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.module.category.CategoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProductTest {

    private static String sampleCategoryId;
    private static String sampleProductIdA;
    private static String sampleProductIdB;
    private static String sampleSupplierId;

    @BeforeAll
    public static void setUpSampleData() {
        com.mycompany.bachhoaxanhonline.config.JpaUtil.getEntityManagerFactory();
        long rand = System.currentTimeMillis() % 1000000;
        sampleSupplierId = "NCC_TEST_" + rand;

        jakarta.persistence.EntityManager em = com.mycompany.bachhoaxanhonline.config.JpaUtil.getEntityManager();
        jakarta.persistence.EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery(
                    "INSERT INTO \"NhaCungCap\" (\"maNhaCungCap\", \"tenNhaCungCap\", \"dangHopTac\", \"Deleted\") "
                    + "VALUES (:id, 'Acecook Viet Nam', true, false)")
                    .setParameter("id", sampleSupplierId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
        }

        CategoryRepository catRepo = new CategoryRepository();
        sampleCategoryId = "LSP_TEST_" + rand;
        catRepo.save(new Category(sampleCategoryId, "Nganh Hang Test " + rand, 0.15, false));

        sampleProductIdA = "SP_TEST_A_" + rand;
        sampleProductIdB = "SP_TEST_B_" + rand;

        ProductRepository prodRepo = new ProductRepository();
        prodRepo.save(new Product(
                sampleProductIdA, "Mi Hao Hao Tom Chua Cay " + rand, "GOI",
                new BigDecimal("4500"), new BigDecimal("3500"), LocalDate.of(2026, 12, 31),
                "https://cdn.bachhoaxanh.com/images/mi-hao-hao.png", 0.08, 150,
                sampleCategoryId, sampleSupplierId, false
        ));

        prodRepo.save(new Product(
                sampleProductIdB, "Banh Mi Bo " + rand, "CAI",
                new BigDecimal("12000"), new BigDecimal("9000"), LocalDate.of(2026, 10, 15),
                "https://cdn.bachhoaxanh.com/images/banh-mi.png", 0.08, 0, // out of stock
                sampleCategoryId, sampleSupplierId, false
        ));
    }

    @Test
    public void testGetProductsDefaultPagination() {
        ProductService service = new ProductService();
        ApiResponse<ProductResponse.ProductListData> response =
                service.getProducts(null, null, null, null, null, null);

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("Lấy danh sách sản phẩm thành công", response.getMessage());
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(1, response.getData().getPage());
        Assertions.assertEquals(12, response.getData().getLimit());
        Assertions.assertTrue(response.getData().getTotal() >= 1);
        Assertions.assertNotNull(response.getData().getProducts());
        Assertions.assertFalse(response.getData().getProducts().isEmpty());

        // Verify product structure matches specification
        ProductResponse.ProductItem item = response.getData().getProducts().get(0);
        Assertions.assertNotNull(item.getProductId());
        Assertions.assertNotNull(item.getProductName());
        Assertions.assertNotNull(item.getPrice());
        Assertions.assertNotNull(item.getUnit());
    }

    @Test
    public void testGetProductsFilterAndSort() {
        ProductService service = new ProductService();

        // 1. Filter by categoryId
        ApiResponse<ProductResponse.ProductListData> catResp =
                service.getProducts("1", "10", null, sampleCategoryId, null, null);
        Assertions.assertEquals(200, catResp.getStatus());
        Assertions.assertEquals(2, catResp.getData().getTotal());

        // 2. Filter inStock = true -> should only return 1 (stock 150 > 0)
        ApiResponse<ProductResponse.ProductListData> inStockResp =
                service.getProducts("1", "10", null, sampleCategoryId, null, "true");
        Assertions.assertEquals(200, inStockResp.getStatus());
        Assertions.assertEquals(1, inStockResp.getData().getTotal());
        Assertions.assertEquals(150, inStockResp.getData().getProducts().get(0).getStock());

        // 3. Sort by price_asc
        ApiResponse<ProductResponse.ProductListData> ascResp =
                service.getProducts("1", "10", null, sampleCategoryId, "price_asc", null);
        Assertions.assertEquals(200, ascResp.getStatus());
        Assertions.assertEquals(2, ascResp.getData().getProducts().size());
        Assertions.assertTrue(ascResp.getData().getProducts().get(0).getPrice()
                .compareTo(ascResp.getData().getProducts().get(1).getPrice()) <= 0);

        // 4. Sort by price_desc
        ApiResponse<ProductResponse.ProductListData> descResp =
                service.getProducts("1", "10", null, sampleCategoryId, "price_desc", null);
        Assertions.assertEquals(200, descResp.getStatus());
        Assertions.assertEquals(2, descResp.getData().getProducts().size());
        Assertions.assertTrue(descResp.getData().getProducts().get(0).getPrice()
                .compareTo(descResp.getData().getProducts().get(1).getPrice()) >= 0);

        // 5. Filter by keyword
        ApiResponse<ProductResponse.ProductListData> keywordResp =
                service.getProducts("1", "10", "Hao Hao", sampleCategoryId, null, null);
        Assertions.assertEquals(200, keywordResp.getStatus());
        Assertions.assertEquals(1, keywordResp.getData().getTotal());
        Assertions.assertTrue(keywordResp.getData().getProducts().get(0).getProductName().contains("Hao Hao"));
    }

    @Test
    public void testQueryParamValidation_BadRequest() {
        ProductService service = new ProductService();

        // 1. page = 0 -> 400
        ProductService.ProductException exPageZero = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProducts("0", "12", null, null, null, null));
        Assertions.assertEquals(400, exPageZero.getStatusCode());

        // 2. page = "invalid" -> 400
        ProductService.ProductException exPageNaN = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProducts("abc", "12", null, null, null, null));
        Assertions.assertEquals(400, exPageNaN.getStatusCode());

        // 3. limit = 0 -> 400
        ProductService.ProductException exLimitZero = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProducts("1", "0", null, null, null, null));
        Assertions.assertEquals(400, exLimitZero.getStatusCode());

        // 4. limit > 100 -> 400
        ProductService.ProductException exLimitMax = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProducts("1", "150", null, null, null, null));
        Assertions.assertEquals(400, exLimitMax.getStatusCode());

        // 5. sortBy invalid -> 400
        ProductService.ProductException exSort = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProducts("1", "12", null, null, "invalid_sort", null));
        Assertions.assertEquals(400, exSort.getStatusCode());

        // 6. inStock invalid -> 400
        ProductService.ProductException exStock = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProducts("1", "12", null, null, null, "maybe"));
        Assertions.assertEquals(400, exStock.getStatusCode());
    }

    @Test
    public void testGetProductDetail_Success() {
        ProductService service = new ProductService();
        ApiResponse<ProductResponse.ProductDetailData> response =
                service.getProductDetail(sampleProductIdA);

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("Lấy chi tiết sản phẩm thành công", response.getMessage());
        Assertions.assertNotNull(response.getData());

        ProductResponse.ProductDetailData detail = response.getData();
        Assertions.assertEquals(sampleProductIdA, detail.getProductId());
        Assertions.assertTrue(detail.getProductName().contains("Mi Hao Hao"));
        Assertions.assertNotNull(detail.getCategory());
        Assertions.assertEquals(sampleCategoryId, detail.getCategory().getCategoryId());
        Assertions.assertNotNull(detail.getSupplier());
        Assertions.assertEquals(sampleSupplierId, detail.getSupplier().getSupplierId());
        Assertions.assertEquals("Acecook Viet Nam", detail.getSupplier().getSupplierName());
        Assertions.assertEquals(0, new BigDecimal("4500").compareTo(detail.getPrice()));
        Assertions.assertEquals(0.08, detail.getVat());
        Assertions.assertEquals("GOI", detail.getUnit());
        Assertions.assertEquals(150, detail.getStock());
        Assertions.assertEquals(LocalDate.of(2026, 12, 31), detail.getExpiryDate());
        Assertions.assertNotNull(detail.getRatingAverage());
        Assertions.assertNotNull(detail.getTotalReviews());
    }

    @Test
    public void testGetProductDetail_NotFoundAndInvalid() {
        ProductService service = new ProductService();

        // 1. Not found -> 404
        ProductService.ProductException ex404 = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProductDetail("NON_EXISTING_PROD_ID"));
        Assertions.assertEquals(404, ex404.getStatusCode());
        Assertions.assertEquals("Sản phẩm không tồn tại hoặc đã bị xóa mềm.", ex404.getMessage());

        // 2. Empty ID -> 400
        ProductService.ProductException ex400 = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProductDetail("  "));
        Assertions.assertEquals(400, ex400.getStatusCode());
    }

    @Test
    public void testCreateProduct_Success() {
        ProductService service = new ProductService();
        long rand = System.currentTimeMillis();
        String newId = "SP_CREATE_" + rand;
        String separateCatId = "LSP_FOR_CREATE_" + rand;

        CategoryRepository catRepo = new CategoryRepository();
        catRepo.save(new Category(separateCatId, "Nganh Hang Rieng " + rand, 0.1, false));

        ProductRequest.CreateProductRequest req = new ProductRequest.CreateProductRequest(
                newId,
                "Sữa Tươi Tiệt Trùng Vinamilk 1L",
                "https://cdn.bachhoaxanh.com/vinamilk-1l.png",
                LocalDate.of(2026, 12, 31),
                separateCatId,
                sampleSupplierId,
                new BigDecimal("28000"),
                0.08,
                new BigDecimal("35000"),
                "CHAI",
                100
        );

        ApiResponse<ProductResponse.CreateProductData> response = service.createProduct(req);
        Assertions.assertEquals(201, response.getStatus());
        Assertions.assertEquals("Tạo sản phẩm / nhập lô hàng mới thành công", response.getMessage());
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(newId, response.getData().getProductId());
        Assertions.assertEquals("Sữa Tươi Tiệt Trùng Vinamilk 1L", response.getData().getProductName());
        Assertions.assertEquals(0, new BigDecimal("35000").compareTo(response.getData().getSellingPrice()));
        Assertions.assertEquals(100, response.getData().getQuantity());
        Assertions.assertEquals("CHAI", response.getData().getUnit());
        Assertions.assertEquals(LocalDate.of(2026, 12, 31), response.getData().getExpiryDate());

        // Verify in DB
        ProductRepository repo = new ProductRepository();
        Product saved = repo.findById(newId).orElse(null);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals(0, new BigDecimal("28000").compareTo(saved.getGiaNhap()));
    }

    @Test
    public void testCreateProduct_ValidationFailures() {
        ProductService service = new ProductService();

        // 1. Missing productId
        ProductRequest.CreateProductRequest reqNoId = new ProductRequest.CreateProductRequest(
                "", "Ten SP", null, null, sampleCategoryId, sampleSupplierId,
                new BigDecimal("10000"), 0.08, new BigDecimal("12000"), "CHAI", 10
        );
        ProductService.ProductException exNoId = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.createProduct(reqNoId));
        Assertions.assertEquals(400, exNoId.getStatusCode());

        // 2. sellingPrice < importPrice -> 400
        ProductRequest.CreateProductRequest reqInvalidPrice = new ProductRequest.CreateProductRequest(
                "SP_INV_PRICE_" + System.currentTimeMillis(), "Ten SP", null, null,
                sampleCategoryId, sampleSupplierId,
                new BigDecimal("20000"), 0.08, new BigDecimal("15000"), "CHAI", 10
        );
        ProductService.ProductException exPrice = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.createProduct(reqInvalidPrice));
        Assertions.assertEquals(400, exPrice.getStatusCode());
        Assertions.assertTrue(exPrice.getMessage().contains("sellingPrice"));

        // 3. Invalid unit -> 400
        ProductRequest.CreateProductRequest reqInvalidUnit = new ProductRequest.CreateProductRequest(
                "SP_INV_UNIT_" + System.currentTimeMillis(), "Ten SP", null, null,
                sampleCategoryId, sampleSupplierId,
                new BigDecimal("10000"), 0.08, new BigDecimal("12000"), "HOP_GIAY", 10
        );
        ProductService.ProductException exUnit = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.createProduct(reqInvalidUnit));
        Assertions.assertEquals(400, exUnit.getStatusCode());
        Assertions.assertTrue(exUnit.getMessage().contains("enum_donvitinh"));

        // 4. Duplicate productId -> 409
        ProductRequest.CreateProductRequest reqDup = new ProductRequest.CreateProductRequest(
                sampleProductIdA, "Ten SP Duplicate", null, null,
                sampleCategoryId, sampleSupplierId,
                new BigDecimal("10000"), 0.08, new BigDecimal("12000"), "CHAI", 10
        );
        ProductService.ProductException exDup = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.createProduct(reqDup));
        Assertions.assertEquals(409, exDup.getStatusCode());
        Assertions.assertTrue(exDup.getMessage().contains("đã tồn tại"));
    }

    @Test
    public void testUpdateProduct_SuccessAndPriceLock() {
        ProductService service = new ProductService();
        ProductRepository repo = new ProductRepository();

        // Check before update
        Product before = repo.findById(sampleProductIdB).orElseThrow();
        BigDecimal originalGiaBan = before.getGiaBan();
        BigDecimal originalGiaNhap = before.getGiaNhap();
        Integer originalSoLuong = before.getSoLuong();

        ProductRequest.UpdateProductRequest updateReq = new ProductRequest.UpdateProductRequest(
                "Banh Mi Bo Nuong Bo To (Vi Moi)",
                "https://cdn.bachhoaxanh.com/images/banh-mi-new.png",
                sampleCategoryId,
                sampleSupplierId,
                "CAI",
                LocalDate.of(2027, 6, 30)
        );

        ApiResponse<ProductResponse.UpdateProductData> response =
                service.updateProduct(sampleProductIdB, updateReq);

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("Cập nhật sản phẩm thành công", response.getMessage());
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(sampleProductIdB, response.getData().getProductId());
        Assertions.assertEquals("Banh Mi Bo Nuong Bo To (Vi Moi)", response.getData().getProductName());
        Assertions.assertEquals("https://cdn.bachhoaxanh.com/images/banh-mi-new.png", response.getData().getImageUrl());
        Assertions.assertEquals("CAI", response.getData().getUnit());
        Assertions.assertEquals(LocalDate.of(2027, 6, 30), response.getData().getExpiryDate());

        // CRITICAL CHECK: Verify price, import price, and quantity in DB did NOT change!
        Product after = repo.findById(sampleProductIdB).orElseThrow();
        Assertions.assertEquals("Banh Mi Bo Nuong Bo To (Vi Moi)", after.getTenSanPham());
        Assertions.assertEquals(0, originalGiaBan.compareTo(after.getGiaBan()));
        Assertions.assertEquals(0, originalGiaNhap.compareTo(after.getGiaNhap()));
        Assertions.assertEquals(originalSoLuong, after.getSoLuong());
    }

    @Test
    public void testUpdateProduct_NotFoundAndValidation() {
        ProductService service = new ProductService();

        // 1. Not found -> 404
        ProductRequest.UpdateProductRequest updateReq = new ProductRequest.UpdateProductRequest(
                "SP Update", null, sampleCategoryId, sampleSupplierId, "CAI", null
        );
        ProductService.ProductException ex404 = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.updateProduct("NON_EXISTENT_ID", updateReq));
        Assertions.assertEquals(404, ex404.getStatusCode());

        // 2. Invalid unit -> 400
        ProductRequest.UpdateProductRequest reqInvUnit = new ProductRequest.UpdateProductRequest(
                "SP Update", null, sampleCategoryId, sampleSupplierId, "INVALID_UNIT", null
        );
        ProductService.ProductException ex400Unit = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.updateProduct(sampleProductIdA, reqInvUnit));
        Assertions.assertEquals(400, ex400Unit.getStatusCode());
    }

    @Test
    public void testDeleteProduct_Success() {
        ProductService service = new ProductService();
        ProductRepository repo = new ProductRepository();
        long rand = System.currentTimeMillis();
        String deleteProdId = "SP_DEL_" + rand;

        // Create product to be deleted
        repo.save(new Product(
                deleteProdId, "San Pham Can Xoa " + rand, "CHAI",
                new BigDecimal("25000"), new BigDecimal("20000"), LocalDate.of(2026, 12, 31),
                null, 0.08, 50, sampleCategoryId, sampleSupplierId, false
        ));

        // Call deleteProduct
        ApiResponse<Void> response = service.deleteProduct(deleteProdId);
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("Xóa sản phẩm thành công", response.getMessage());

        // Verify in DB that it is soft-deleted (Deleted = true)
        Product deletedInDb = repo.findById(deleteProdId).orElse(null);
        Assertions.assertNotNull(deletedInDb);
        Assertions.assertTrue(deletedInDb.getDeleted());

        // Verify that getProductDetail returns 404 (hidden from public view)
        ProductService.ProductException ex404 = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.getProductDetail(deleteProdId));
        Assertions.assertEquals(404, ex404.getStatusCode());
    }

    @Test
    public void testDeleteProduct_AlreadyDeletedOrNotFound() {
        ProductService service = new ProductService();
        ProductRepository repo = new ProductRepository();
        long rand = System.currentTimeMillis();
        String alreadyDeletedId = "SP_ALREADY_DEL_" + rand;

        // Pre-create an already soft-deleted product
        repo.save(new Product(
                alreadyDeletedId, "San Pham Da Xoa " + rand, "CHAI",
                new BigDecimal("25000"), new BigDecimal("20000"), LocalDate.of(2026, 12, 31),
                null, 0.08, 50, sampleCategoryId, sampleSupplierId, true // deleted = true
        ));

        // 1. Trying to delete already deleted product -> 404
        ProductService.ProductException exAlready = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.deleteProduct(alreadyDeletedId));
        Assertions.assertEquals(404, exAlready.getStatusCode());
        Assertions.assertTrue(exAlready.getMessage().contains("đã bị xóa trước đó"));

        // 2. Trying to delete non-existent ID -> 404
        ProductService.ProductException exNotFound = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.deleteProduct("NON_EXISTING_ID_" + rand));
        Assertions.assertEquals(404, exNotFound.getStatusCode());
        Assertions.assertTrue(exNotFound.getMessage().contains("đã bị xóa trước đó"));

        // 3. Empty ID -> 400
        ProductService.ProductException exEmpty = Assertions.assertThrows(
                ProductService.ProductException.class, () -> service.deleteProduct("   "));
        Assertions.assertEquals(400, exEmpty.getStatusCode());
    }
}
