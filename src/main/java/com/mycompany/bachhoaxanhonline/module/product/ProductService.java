package com.mycompany.bachhoaxanhonline.module.product;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.module.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductService {

    public static final Set<String> ALLOWED_UNITS = Set.of(
            "LON", "CHAI", "LOC4", "LOC6", "THUNG24", "THUNG30", "THUNG48", "GOI", "CAI", "BAO", "KG"
    );

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
        this.categoryRepository = new CategoryRepository();
    }

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = new CategoryRepository();
    }

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public static class ProductException extends RuntimeException {
        private final int statusCode;

        public ProductException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public ApiResponse<ProductResponse.ProductListData> getProducts(
            String pageStr, String limitStr, String keyword, String categoryId, String sortBy, String inStockStr) {

        int page = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr.trim());
                if (page < 1) {
                    throw new ProductException(400, "Tham số query sai định dạng: page phải là số nguyên dương >= 1");
                }
            } catch (NumberFormatException e) {
                throw new ProductException(400, "Tham số query sai định dạng: page phải là số nguyên");
            }
        }

        int limit = 12;
        if (limitStr != null && !limitStr.trim().isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr.trim());
                if (limit < 1 || limit > 100) {
                    throw new ProductException(400, "Tham số query sai định dạng: limit phải là số nguyên từ 1 đến 100");
                }
            } catch (NumberFormatException e) {
                throw new ProductException(400, "Tham số query sai định dạng: limit phải là số nguyên");
            }
        }

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            sortBy = sortBy.trim();
            if (!"price_asc".equalsIgnoreCase(sortBy) && !"price_desc".equalsIgnoreCase(sortBy) && !"newest".equalsIgnoreCase(sortBy)) {
                throw new ProductException(400, "Tham số query sai định dạng: sortBy chỉ chấp nhận 'price_asc', 'price_desc', 'newest'");
            }
        } else {
            sortBy = null;
        }

        Boolean inStock = null;
        if (inStockStr != null && !inStockStr.trim().isEmpty()) {
            String trimmedInStock = inStockStr.trim();
            if ("true".equalsIgnoreCase(trimmedInStock)) {
                inStock = true;
            } else if ("false".equalsIgnoreCase(trimmedInStock)) {
                inStock = false;
            } else {
                throw new ProductException(400, "Tham số query sai định dạng: inStock phải là boolean ('true' hoặc 'false')");
            }
        }

        String cleanedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanedCategoryId = (categoryId != null && !categoryId.trim().isEmpty()) ? categoryId.trim() : null;

        long total = productRepository.countProducts(cleanedKeyword, cleanedCategoryId, inStock);
        List<Product> products = productRepository.findProducts(page, limit, cleanedKeyword, cleanedCategoryId, sortBy, inStock);

        List<ProductResponse.ProductItem> productItems = products.stream()
                .map(this::mapToProductItem)
                .collect(Collectors.toList());

        ProductResponse.ProductListData listData = new ProductResponse.ProductListData(total, page, limit, productItems);

        return new ApiResponse<>(200, "Lấy danh sách sản phẩm thành công", listData);
    }

    private ProductResponse.ProductItem mapToProductItem(Product p) {
        ProductResponse.CategoryInfo categoryInfo = null;
        if (p.getCategory() != null) {
            categoryInfo = new ProductResponse.CategoryInfo(
                    p.getCategory().getMaLoaiSanPham(),
                    p.getCategory().getTenLoaiSanPham()
            );
        } else if (p.getMaLoai() != null) {
            categoryInfo = new ProductResponse.CategoryInfo(p.getMaLoai(), null);
        }

        return new ProductResponse.ProductItem(
                p.getMaSanPham(),
                p.getTenSanPham(),
                p.getHinhAnh(),
                categoryInfo,
                p.getGiaBan(),
                p.getDonViTinh(),
                p.getSoLuong(),
                p.getHanSuDung()
        );
    }

    public ApiResponse<ProductResponse.ProductDetailData> getProductDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ProductException(400, "Mã sản phẩm không được để trống");
        }

        Optional<Product> optProduct = productRepository.findActiveProductById(id.trim());
        if (optProduct.isEmpty()) {
            throw new ProductException(404, "Sản phẩm không tồn tại hoặc đã bị xóa mềm.");
        }

        Product p = optProduct.get();

        ProductResponse.CategoryInfo categoryInfo = null;
        if (p.getCategory() != null) {
            categoryInfo = new ProductResponse.CategoryInfo(
                    p.getCategory().getMaLoaiSanPham(),
                    p.getCategory().getTenLoaiSanPham()
            );
        } else if (p.getMaLoai() != null) {
            categoryInfo = new ProductResponse.CategoryInfo(p.getMaLoai(), null);
        }

        String supplierName = productRepository.findSupplierNameById(p.getMaNhaCungCap());
        ProductResponse.SupplierInfo supplierInfo = new ProductResponse.SupplierInfo(
                p.getMaNhaCungCap(),
                supplierName
        );

        ProductRepository.RatingSummary rating = productRepository.getRatingSummary(p.getMaSanPham());

        ProductResponse.ProductDetailData detailData = new ProductResponse.ProductDetailData(
                p.getMaSanPham(),
                p.getTenSanPham(),
                p.getHinhAnh(),
                categoryInfo,
                supplierInfo,
                p.getGiaBan(),
                p.getPhiVAT(),
                p.getDonViTinh(),
                p.getSoLuong(),
                p.getHanSuDung(),
                rating.getRatingAverage(),
                rating.getTotalReviews()
        );

        return new ApiResponse<>(200, "Lấy chi tiết sản phẩm thành công", detailData);
    }

    public ApiResponse<ProductResponse.CreateProductData> createProduct(ProductRequest.CreateProductRequest request) {
        if (request == null) {
            throw new ProductException(400, "Dữ liệu yêu cầu không được để trống");
        }
        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: productId không được để trống");
        }
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: productName không được để trống");
        }
        if (request.getCategoryId() == null || request.getCategoryId().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: categoryId không được để trống");
        }
        if (request.getSupplierId() == null || request.getSupplierId().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: supplierId không được để trống");
        }
        if (request.getUnit() == null || request.getUnit().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: unit không được để trống");
        }

        String unit = request.getUnit().trim().toUpperCase();
        if (!ALLOWED_UNITS.contains(unit)) {
            throw new ProductException(400, "Đơn vị tính không nằm trong enum_donvitinh. Cho phép: " + String.join(", ", ALLOWED_UNITS));
        }

        if (request.getImportPrice() == null || request.getImportPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: importPrice không hợp lệ (phải >= 0)");
        }
        if (request.getSellingPrice() == null || request.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: sellingPrice không hợp lệ (phải >= 0)");
        }
        if (request.getSellingPrice().compareTo(request.getImportPrice()) < 0) {
            throw new ProductException(400, "Giá bán (sellingPrice) phải lớn hơn hoặc bằng giá nhập (importPrice) theo quy định.");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: quantity không hợp lệ (phải >= 0)");
        }

        Double vat = request.getVat() != null ? request.getVat() : 0.08;
        if (vat < 0) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: vat không hợp lệ");
        }

        String productId = request.getProductId().trim();
        if (productRepository.existsById(productId)) {
            throw new ProductException(409, "Mã sản phẩm/mã lô (productId) đã tồn tại trong hệ thống.");
        }

        if (!categoryRepository.existsById(request.getCategoryId().trim())) {
            throw new ProductException(400, "Loại sản phẩm không tồn tại trong hệ thống: " + request.getCategoryId());
        }
        if (!productRepository.existsSupplierById(request.getSupplierId().trim())) {
            throw new ProductException(400, "Nhà cung cấp không tồn tại trong hệ thống: " + request.getSupplierId());
        }

        Product product = new Product(
                productId,
                request.getProductName().trim(),
                unit,
                request.getSellingPrice(),
                request.getImportPrice(),
                request.getExpiryDate(),
                request.getImageUrl() != null ? request.getImageUrl().trim() : null,
                vat,
                request.getQuantity(),
                request.getCategoryId().trim(),
                request.getSupplierId().trim(),
                false
        );
        productRepository.save(product);

        ProductResponse.CreateProductData data = new ProductResponse.CreateProductData(
                product.getMaSanPham(),
                product.getTenSanPham(),
                product.getGiaBan(),
                product.getSoLuong(),
                product.getDonViTinh(),
                product.getHanSuDung()
        );

        return new ApiResponse<>(201, "Tạo sản phẩm / nhập lô hàng mới thành công", data);
    }

    public ApiResponse<ProductResponse.UpdateProductData> updateProduct(String productId, ProductRequest.UpdateProductRequest request) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ProductException(400, "Mã sản phẩm không được để trống");
        }
        if (request == null) {
            throw new ProductException(400, "Dữ liệu yêu cầu không được để trống");
        }
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: productName không được để trống");
        }
        if (request.getCategoryId() == null || request.getCategoryId().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: categoryId không được để trống");
        }
        if (request.getSupplierId() == null || request.getSupplierId().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: supplierId không được để trống");
        }
        if (request.getUnit() == null || request.getUnit().trim().isEmpty()) {
            throw new ProductException(400, "Thiếu thông tin bắt buộc: unit không được để trống");
        }

        String unit = request.getUnit().trim().toUpperCase();
        if (!ALLOWED_UNITS.contains(unit)) {
            throw new ProductException(400, "Đơn vị tính không nằm trong enum_donvitinh. Cho phép: " + String.join(", ", ALLOWED_UNITS));
        }

        Optional<Product> optProduct = productRepository.findActiveProductById(productId.trim());
        if (optProduct.isEmpty()) {
            throw new ProductException(404, "Không tìm thấy sản phẩm.");
        }

        if (!categoryRepository.existsById(request.getCategoryId().trim())) {
            throw new ProductException(400, "Loại sản phẩm không tồn tại trong hệ thống: " + request.getCategoryId());
        }
        if (!productRepository.existsSupplierById(request.getSupplierId().trim())) {
            throw new ProductException(400, "Nhà cung cấp không tồn tại trong hệ thống: " + request.getSupplierId());
        }

        productRepository.updateProductMetadata(
                productId.trim(),
                request.getProductName().trim(),
                request.getImageUrl() != null ? request.getImageUrl().trim() : null,
                request.getCategoryId().trim(),
                request.getSupplierId().trim(),
                unit,
                request.getExpiryDate()
        );

        ProductResponse.UpdateProductData data = new ProductResponse.UpdateProductData(
                productId.trim(),
                request.getProductName().trim(),
                request.getImageUrl() != null ? request.getImageUrl().trim() : null,
                unit,
                request.getExpiryDate()
        );

        return new ApiResponse<>(200, "Cập nhật sản phẩm thành công", data);
    }

    public ApiResponse<Void> deleteProduct(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ProductException(400, "Mã sản phẩm không được để trống");
        }

        boolean deleted = productRepository.softDelete(id.trim());
        if (!deleted) {
            throw new ProductException(404, "Không tìm thấy sản phẩm hoặc sản phẩm này vốn đã bị xóa trước đó.");
        }

        return new ApiResponse<>(200, "Xóa sản phẩm thành công");
    }
}
