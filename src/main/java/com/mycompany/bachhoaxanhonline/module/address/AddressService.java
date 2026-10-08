package com.mycompany.bachhoaxanhonline.module.address;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService() {
        this.addressRepository = new AddressRepository();
    }

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public static class AddressException extends RuntimeException {
        private final int statusCode;

        public AddressException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public boolean isAccountLockedOrNotCustomer(String customerId) {
        return !addressRepository.isCustomerActive(customerId);
    }

    public ApiResponse<List<AddressResponse.AddressData>> getAddresses(String customerId) {
        if (isAccountLockedOrNotCustomer(customerId)) {
            throw new AddressException(403, "Tài khoản bị khóa hoặc không phải role khách hàng");
        }

        List<Address> addresses = addressRepository.findActiveByCustomerId(customerId);
        List<AddressResponse.AddressData> items = addresses.stream()
                .map(this::mapToAddressData)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Lấy danh sách địa chỉ thành công", items);
    }

    public ApiResponse<AddressResponse.AddressData> createAddress(
            String customerId, AddressRequest.CreateAddressRequest request) {

        if (isAccountLockedOrNotCustomer(customerId)) {
            throw new AddressException(403, "Tài khoản bị khóa hoặc không phải role khách hàng");
        }

        if (request == null
                || isBlank(request.getReceiverName())
                || isBlank(request.getPhoneNumber())
                || isBlank(request.getStreet())
                || isBlank(request.getCity())) {
            throw new AddressException(400, "Thiếu các trường bắt buộc (receiverName, phoneNumber, street, city)");
        }

        long currentCount = addressRepository.countByCustomerId(customerId);
        if (currentCount >= 5) {
            throw new AddressException(400, "Bạn chỉ được lưu tối đa 5 địa chỉ nhận hàng. Vui lòng xóa bớt địa chỉ cũ để thêm mới.");
        }

        boolean isFirst = (currentCount == 0);
        boolean isDefault = isFirst || Boolean.TRUE.equals(request.getIsDefault());

        Address address = new Address(
                null,
                request.getReceiverName().trim(),
                request.getPhoneNumber().trim(),
                request.getStreet().trim(),
                request.getWard() != null ? request.getWard().trim() : "",
                request.getCity().trim(),
                isDefault,
                false,
                customerId
        );

        try {
            Address created = addressRepository.insertAddress(address, isDefault && !isFirst);
            return new ApiResponse<>(201, "Thêm địa chỉ thành công", mapToAddressData(created));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("tối đa 5 địa chỉ")) {
                throw new AddressException(400, "Bạn chỉ được lưu tối đa 5 địa chỉ nhận hàng. Vui lòng xóa bớt địa chỉ cũ để thêm mới.");
            }
            throw e;
        }
    }

    public ApiResponse<AddressResponse.AddressData> updateAddress(
            String customerId, Long addressId, AddressRequest.UpdateAddressRequest request) {

        if (addressId == null) {
            throw new AddressException(400, "Mã địa chỉ không hợp lệ");
        }

        if (isAccountLockedOrNotCustomer(customerId)) {
            throw new AddressException(403, "Tài khoản bị khóa hoặc không phải role khách hàng");
        }

        if (request == null
                || isBlank(request.getReceiverName())
                || isBlank(request.getPhoneNumber())
                || isBlank(request.getStreet())
                || isBlank(request.getCity())) {
            throw new AddressException(400, "Dữ liệu gửi lên thiếu hoặc không hợp lệ");
        }

        Optional<Address> optAddress = addressRepository.findById(addressId);
        if (optAddress.isEmpty() || Boolean.TRUE.equals(optAddress.get().getDeleted())) {
            throw new AddressException(404, "Không tìm thấy địa chỉ (hoặc địa chỉ đã bị xóa mềm)");
        }

        Address existing = optAddress.get();
        if (!customerId.equals(existing.getMaKhachHang())) {
            throw new AddressException(403, "Khách hàng cố tình sửa địa chỉ không thuộc quyền sở hữu của mình");
        }

        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());

        Address updated = addressRepository.updateAddress(
                addressId,
                customerId,
                request.getReceiverName().trim(),
                request.getPhoneNumber().trim(),
                request.getStreet().trim(),
                request.getWard() != null ? request.getWard().trim() : "",
                request.getCity().trim(),
                isDefault
        );

        return new ApiResponse<>(200, "Cập nhật địa chỉ thành công", mapToAddressData(updated));
    }

    public ApiResponse<AddressResponse.DefaultAddressData> setDefaultAddress(
            String customerId, Long addressId) {

        if (addressId == null) {
            throw new AddressException(400, "Mã địa chỉ không hợp lệ");
        }

        if (isAccountLockedOrNotCustomer(customerId)) {
            throw new AddressException(403, "Tài khoản bị khóa hoặc không phải role khách hàng");
        }

        Optional<Address> optAddress = addressRepository.findById(addressId);
        if (optAddress.isEmpty() || Boolean.TRUE.equals(optAddress.get().getDeleted())) {
            throw new AddressException(404, "Không tìm thấy địa chỉ");
        }

        Address existing = optAddress.get();
        if (!customerId.equals(existing.getMaKhachHang())) {
            throw new AddressException(403, "Địa chỉ không thuộc sở hữu của khách hàng đang đăng nhập");
        }

        addressRepository.setDefaultAddress(addressId, customerId);

        return new ApiResponse<>(200, "Đặt địa chỉ mặc định thành công",
                new AddressResponse.DefaultAddressData(addressId, true));
    }

    public ApiResponse<Void> deleteAddress(String customerId, Long addressId) {
        if (addressId == null) {
            throw new AddressException(400, "Mã địa chỉ không hợp lệ");
        }

        if (isAccountLockedOrNotCustomer(customerId)) {
            throw new AddressException(403, "Tài khoản bị khóa hoặc không phải role khách hàng");
        }

        Optional<Address> optAddress = addressRepository.findById(addressId);
        if (optAddress.isEmpty() || Boolean.TRUE.equals(optAddress.get().getDeleted())) {
            throw new AddressException(404, "Không tìm thấy địa chỉ với mã ID tương ứng trong cơ sở dữ liệu");
        }

        Address existing = optAddress.get();
        if (!customerId.equals(existing.getMaKhachHang())) {
            throw new AddressException(403, "Địa chỉ muốn xóa không thuộc quyền sở hữu của khách hàng đang đăng nhập");
        }

        boolean wasDefault = Boolean.TRUE.equals(existing.getLaMacDinh());
        addressRepository.deleteAddress(addressId, customerId, wasDefault);

        return new ApiResponse<>(200, "Xóa địa chỉ nhận hàng thành công");
    }

    private AddressResponse.AddressData mapToAddressData(Address a) {
        return new AddressResponse.AddressData(
                a.getMaDiaChi(),
                a.getTenNguoiNhan(),
                a.getSoDienThoai(),
                a.getSoNha(),
                a.getPhuong(),
                a.getTinh(),
                a.getLaMacDinh()
        );
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
