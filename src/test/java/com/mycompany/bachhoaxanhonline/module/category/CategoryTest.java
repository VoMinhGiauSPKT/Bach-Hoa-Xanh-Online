package com.mycompany.bachhoaxanhonline.module.category;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryTest {

    @Test
    public void testGetPublicCategories() {
        CategoryService service = new CategoryService();
        ApiResponse<List<CategoryResponse.CategoryPublicItem>> response = service.getPublicCategories();

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().isEmpty());

        CategoryResponse.CategoryPublicItem first = response.getData().get(0);
        Assertions.assertNotNull(first.getCategoryId());
        Assertions.assertNotNull(first.getCategoryName());
    }

    @Test
    public void testCreateCategorySuccessAndConflict() {
        CategoryService service = new CategoryService();
        long rand = System.currentTimeMillis() % 1000000;
        String catId = "LSP_TEST_" + rand;
        String catName = "Test Loai " + rand;
        Double margin = 0.15;

        // 1. Tạo thành công (201)
        CategoryRequest.CreateCategoryRequest createReq = new CategoryRequest.CreateCategoryRequest(catId, catName, margin);
        ApiResponse<CategoryResponse.CategoryData> createResp = service.createCategory(createReq);

        Assertions.assertEquals(201, createResp.getStatus());
        Assertions.assertEquals(catId, createResp.getData().getCategoryId());
        Assertions.assertEquals(catName, createResp.getData().getCategoryName());
        Assertions.assertEquals(margin, createResp.getData().getProfitMargin());
        Assertions.assertFalse(createResp.getData().getDeleted());

        // 2. Trùng ID -> 409
        CategoryRequest.CreateCategoryRequest dupIdReq = new CategoryRequest.CreateCategoryRequest(catId, "Khac Ten " + rand, 0.2);
        CategoryService.CategoryException exId = Assertions.assertThrows(
                CategoryService.CategoryException.class, () -> service.createCategory(dupIdReq));
        Assertions.assertEquals(409, exId.getStatusCode());

        // 3. Trùng Name -> 409
        CategoryRequest.CreateCategoryRequest dupNameReq = new CategoryRequest.CreateCategoryRequest("LSP_OTHER_" + rand, catName, 0.2);
        CategoryService.CategoryException exName = Assertions.assertThrows(
                CategoryService.CategoryException.class, () -> service.createCategory(dupNameReq));
        Assertions.assertEquals(409, exName.getStatusCode());

        // 4. Dữ liệu rỗng / không hợp lệ -> 400
        CategoryRequest.CreateCategoryRequest invalidReq = new CategoryRequest.CreateCategoryRequest("", -0.5);
        CategoryService.CategoryException exInvalid = Assertions.assertThrows(
                CategoryService.CategoryException.class, () -> service.createCategory(invalidReq));
        Assertions.assertEquals(400, exInvalid.getStatusCode());
    }

    @Test
    public void testGetCategoryDetailAndNotFound() {
        CategoryService service = new CategoryService();
        long rand = System.currentTimeMillis() % 1000000;
        String catId = "LSP_DET_" + rand;
        String catName = "Loai Detail " + rand;

        service.createCategory(new CategoryRequest.CreateCategoryRequest(catId, catName, 0.25));

        // 1. Tìm thấy -> 200
        ApiResponse<CategoryResponse.CategoryData> detailResp = service.getCategoryDetail(catId);
        Assertions.assertEquals(200, detailResp.getStatus());
        Assertions.assertEquals(catId, detailResp.getData().getCategoryId());
        Assertions.assertEquals(catName, detailResp.getData().getCategoryName());
        Assertions.assertEquals(0.25, detailResp.getData().getProfitMargin());

        // 2. Không tồn tại -> 404
        CategoryService.CategoryException exNotFound = Assertions.assertThrows(
                CategoryService.CategoryException.class, () -> service.getCategoryDetail("NON_EXISTING_ID"));
        Assertions.assertEquals(404, exNotFound.getStatusCode());
    }

    @Test
    public void testUpdateCategory() {
        CategoryService service = new CategoryService();
        long rand = System.currentTimeMillis() % 1000000;
        String catId = "LSP_UPD_" + rand;
        String catName = "Loai PreUpdate " + rand;

        service.createCategory(new CategoryRequest.CreateCategoryRequest(catId, catName, 0.1));

        // 1. Cập nhật thành công -> 200
        String newName = "Loai PostUpdate " + rand;
        CategoryRequest.UpdateCategoryRequest updateReq = new CategoryRequest.UpdateCategoryRequest(newName, 0.18);
        ApiResponse<CategoryResponse.CategoryData> updateResp = service.updateCategory(catId, updateReq);

        Assertions.assertEquals(200, updateResp.getStatus());
        Assertions.assertEquals(newName, updateResp.getData().getCategoryName());
        Assertions.assertEquals(0.18, updateResp.getData().getProfitMargin());

        // 2. Cập nhật không tìm thấy ID -> 404
        CategoryService.CategoryException exNotFound = Assertions.assertThrows(
                CategoryService.CategoryException.class, () -> service.updateCategory("NON_EXIST_ID", updateReq));
        Assertions.assertEquals(404, exNotFound.getStatusCode());
    }

    @Test
    public void testDeleteCategory() {
        CategoryService service = new CategoryService();
        long rand = System.currentTimeMillis() % 1000000;
        String catId = "LSP_DEL_" + rand;
        String catName = "Loai Deletable " + rand;

        service.createCategory(new CategoryRequest.CreateCategoryRequest(catId, catName, 0.12));

        // 1. Xóa mềm thành công -> 200
        ApiResponse<Void> delResp = service.deleteCategory(catId);
        Assertions.assertEquals(200, delResp.getStatus());

        // 2. Sau khi xóa mềm, gọi getDetail -> 404
        CategoryService.CategoryException exAfterDel = Assertions.assertThrows(
                CategoryService.CategoryException.class, () -> service.getCategoryDetail(catId));
        Assertions.assertEquals(404, exAfterDel.getStatusCode());
    }
}
