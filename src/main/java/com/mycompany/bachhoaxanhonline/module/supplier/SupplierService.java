package com.mycompany.bachhoaxanhonline.module.supplier;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.Supplier;
import com.mycompany.bachhoaxanhonline.util.CodeGeneratorUtil;
import com.mycompany.bachhoaxanhonline.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService() {
        this.supplierRepository = new SupplierRepository();
    }

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public static class SupplierException extends RuntimeException {
        private final int statusCode;

        public SupplierException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public ApiResponse<List<SupplierResponse.SupplierItem>> getSuppliers(boolean activeOnly) {
        List<Supplier> suppliers = supplierRepository.findAll(activeOnly);
        List<SupplierResponse.SupplierItem> items = suppliers.stream()
                .map(s -> new SupplierResponse.SupplierItem(
                        s.getMaNhaCungCap(),
                        s.getTenNhaCungCap(),
                        s.getEmail(),
                        s.getSoDienThoai(),
                        s.getDangHopTac(),
                        s.getDeleted()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Lấy danh sách nhà cung cấp thành công", items);
    }

    public ApiResponse<SupplierResponse.SupplierItem> getSupplierDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new SupplierException(400, "Mã nhà cung cấp không được để trống");
        }

        Optional<Supplier> opt = supplierRepository.findById(id.trim());
        if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getDeleted())) {
            throw new SupplierException(404, "Không tìm thấy nhà cung cấp với mã: " + id);
        }

        Supplier s = opt.get();
        SupplierResponse.SupplierItem item = new SupplierResponse.SupplierItem(
                s.getMaNhaCungCap(),
                s.getTenNhaCungCap(),
                s.getEmail(),
                s.getSoDienThoai(),
                s.getDangHopTac(),
                s.getDeleted()
        );

        return new ApiResponse<>(200, "Lấy chi tiết nhà cung cấp thành công", item);
    }

    public ApiResponse<SupplierResponse.SupplierItem> createSupplier(SupplierRequest.CreateSupplierRequest request) {
        if (request == null) {
            throw new SupplierException(400, "Thiếu thông tin nhà cung cấp");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new SupplierException(400, "Dữ liệu không hợp lệ: " + violation);
        }

        String supplierId = request.getSupplierId();
        if (supplierId != null && !supplierId.trim().isEmpty()) {
            supplierId = supplierId.trim().toUpperCase();
            if (supplierRepository.existsById(supplierId)) {
                throw new SupplierException(409, "Mã nhà cung cấp đã tồn tại: " + supplierId);
            }
        } else {
            supplierId = CodeGeneratorUtil.generateNextCode("Supplier", "maNhaCungCap", "NCC", 2);
        }

        String supplierName = request.getSupplierName().trim();
        if (supplierRepository.existsByName(supplierName)) {
            throw new SupplierException(409, "Tên nhà cung cấp đã tồn tại trong hệ thống: " + supplierName);
        }

        Supplier supplier = new Supplier(
                supplierId,
                supplierName,
                request.getEmail() != null ? request.getEmail().trim() : null,
                request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null,
                request.getActive() != null ? request.getActive() : true,
                false
        );

        supplierRepository.save(supplier);

        SupplierResponse.SupplierItem item = new SupplierResponse.SupplierItem(
                supplier.getMaNhaCungCap(),
                supplier.getTenNhaCungCap(),
                supplier.getEmail(),
                supplier.getSoDienThoai(),
                supplier.getDangHopTac(),
                supplier.getDeleted()
        );

        return new ApiResponse<>(201, "Tạo nhà cung cấp thành công", item);
    }

    public ApiResponse<SupplierResponse.SupplierItem> updateSupplier(String id, SupplierRequest.UpdateSupplierRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new SupplierException(400, "Mã nhà cung cấp không được để trống");
        }
        if (request == null) {
            throw new SupplierException(400, "Thiếu thông tin cập nhật");
        }

        String violation = ValidationUtil.getFirstViolationMessage(request);
        if (violation != null) {
            throw new SupplierException(400, "Dữ liệu không hợp lệ: " + violation);
        }

        Optional<Supplier> opt = supplierRepository.findById(id.trim());
        if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getDeleted())) {
            throw new SupplierException(404, "Không tìm thấy nhà cung cấp với mã: " + id);
        }

        String supplierName = request.getSupplierName().trim();
        if (supplierRepository.existsByNameExceptId(supplierName, id.trim())) {
            throw new SupplierException(409, "Tên nhà cung cấp đã được sử dụng bởi nhà cung cấp khác: " + supplierName);
        }

        Supplier supplier = opt.get();
        supplier.setTenNhaCungCap(supplierName);
        supplier.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        supplier.setSoDienThoai(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        if (request.getActive() != null) {
            supplier.setDangHopTac(request.getActive());
        }

        supplierRepository.update(supplier);

        SupplierResponse.SupplierItem item = new SupplierResponse.SupplierItem(
                supplier.getMaNhaCungCap(),
                supplier.getTenNhaCungCap(),
                supplier.getEmail(),
                supplier.getSoDienThoai(),
                supplier.getDangHopTac(),
                supplier.getDeleted()
        );

        return new ApiResponse<>(200, "Cập nhật nhà cung cấp thành công", item);
    }

    public ApiResponse<Void> deleteSupplier(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new SupplierException(400, "Mã nhà cung cấp không được để trống");
        }

        Optional<Supplier> opt = supplierRepository.findById(id.trim());
        if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getDeleted())) {
            throw new SupplierException(404, "Không tìm thấy nhà cung cấp với mã: " + id);
        }

        supplierRepository.softDelete(id.trim());
        return new ApiResponse<>(200, "Đã xóa nhà cung cấp thành công", null);
    }
}
