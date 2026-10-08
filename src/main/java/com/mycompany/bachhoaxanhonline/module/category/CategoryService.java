package com.mycompany.bachhoaxanhonline.module.category;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public static class CategoryException extends RuntimeException {
        private final int statusCode;

        public CategoryException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public ApiResponse<List<CategoryResponse.CategoryPublicItem>> getPublicCategories() {
        List<Category> categories = categoryRepository.findAll(true);
        List<CategoryResponse.CategoryPublicItem> items = categories.stream()
                .map(c -> new CategoryResponse.CategoryPublicItem(c.getMaLoaiSanPham(), c.getTenLoaiSanPham()))
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Lấy danh sách loại sản phẩm thành công", items);
    }

    public ApiResponse<CategoryResponse.CategoryData> getCategoryDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new CategoryException(400, "Mã loại sản phẩm không được để trống");
        }

        Optional<Category> optCategory = categoryRepository.findById(id.trim());
        if (optCategory.isEmpty() || Boolean.TRUE.equals(optCategory.get().getDeleted())) {
            throw new CategoryException(404, "Không tìm thấy loại sản phẩm với mã: " + id);
        }

        Category c = optCategory.get();
        CategoryResponse.CategoryData data = new CategoryResponse.CategoryData(
                c.getMaLoaiSanPham(),
                c.getTenLoaiSanPham(),
                c.getPhanTramLoiNhuan(),
                c.getDeleted()
        );

        return new ApiResponse<>(200, "Lấy chi tiết loại sản phẩm thành công", data);
    }

    public ApiResponse<CategoryResponse.CategoryData> createCategory(CategoryRequest.CreateCategoryRequest request) {
        if (request == null) {
            throw new CategoryException(400, "Thiếu thông tin loại sản phẩm");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new CategoryException(400, "Dữ liệu không hợp lệ: " + violation);
        }

        String categoryId = request.getCategoryId();
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            categoryId = categoryId.trim();
            if (categoryRepository.existsById(categoryId)) {
                throw new CategoryException(409, "Mã loại sản phẩm đã tồn tại: " + categoryId);
            }
        } else {
            categoryId = "LSP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }

        String categoryName = request.getCategoryName().trim();
        if (categoryRepository.existsByName(categoryName)) {
            throw new CategoryException(409, "Tên loại sản phẩm đã tồn tại trong hệ thống: " + categoryName);
        }

        Category category = new Category(categoryId, categoryName, request.getProfitMargin(), false);
        categoryRepository.save(category);

        CategoryResponse.CategoryData data = new CategoryResponse.CategoryData(
                category.getMaLoaiSanPham(),
                category.getTenLoaiSanPham(),
                category.getPhanTramLoiNhuan(),
                category.getDeleted()
        );

        return new ApiResponse<>(201, "Tạo loại sản phẩm thành công", data);
    }

    public ApiResponse<CategoryResponse.CategoryData> updateCategory(String id, CategoryRequest.UpdateCategoryRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new CategoryException(400, "Mã loại sản phẩm không được để trống");
        }
        if (request == null) {
            throw new CategoryException(400, "Thiếu thông tin cập nhật");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new CategoryException(400, "Dữ liệu không hợp lệ: " + violation);
        }

        Optional<Category> optCategory = categoryRepository.findById(id.trim());
        if (optCategory.isEmpty() || Boolean.TRUE.equals(optCategory.get().getDeleted())) {
            throw new CategoryException(404, "Không tìm thấy loại sản phẩm với mã: " + id);
        }

        String categoryName = request.getCategoryName().trim();
        if (categoryRepository.existsByNameExceptId(categoryName, id.trim())) {
            throw new CategoryException(409, "Tên loại sản phẩm đã được sử dụng bởi loại khác: " + categoryName);
        }

        Category category = optCategory.get();
        category.setTenLoaiSanPham(categoryName);
        category.setPhanTramLoiNhuan(request.getProfitMargin());

        categoryRepository.update(category);

        CategoryResponse.CategoryData data = new CategoryResponse.CategoryData(
                category.getMaLoaiSanPham(),
                category.getTenLoaiSanPham(),
                category.getPhanTramLoiNhuan(),
                category.getDeleted()
        );

        return new ApiResponse<>(200, "Cập nhật loại sản phẩm thành công", data);
    }

    public ApiResponse<Void> deleteCategory(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new CategoryException(400, "Mã loại sản phẩm không được để trống");
        }

        Optional<Category> optCategory = categoryRepository.findById(id.trim());
        if (optCategory.isEmpty() || Boolean.TRUE.equals(optCategory.get().getDeleted())) {
            throw new CategoryException(404, "Không tìm thấy loại sản phẩm với mã: " + id);
        }

        long activeProductsCount = categoryRepository.countActiveProductsByCategoryId(id.trim());
        if (activeProductsCount > 0) {
            throw new CategoryException(400, "Không thể xóa loại sản phẩm này vì vẫn còn " + activeProductsCount + " sản phẩm đang kinh doanh");
        }

        categoryRepository.softDelete(id.trim());

        return new ApiResponse<>(200, "Xóa loại sản phẩm thành công");
    }
}
