package com.mycompany.bachhoaxanhonline.module.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

public class UserResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiResponse<T> {
        private int status;
        private String message;
        private T data;

        public ApiResponse() {
        }

        public ApiResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public ApiResponse(int status, String message, T data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddressData {
        private Long addressId;
        private String receiverName;
        private String phoneNumber;
        private String street;
        private String ward;
        private String city;
        private Boolean isDefault;

        public AddressData() {
        }

        public AddressData(Long addressId, String receiverName, String phoneNumber, String street, String ward,
                String city, Boolean isDefault) {
            this.addressId = addressId;
            this.receiverName = receiverName;
            this.phoneNumber = phoneNumber;
            this.street = street;
            this.ward = ward;
            this.city = city;
            this.isDefault = isDefault;
        }

        public Long getAddressId() {
            return addressId;
        }

        public void setAddressId(Long addressId) {
            this.addressId = addressId;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getWard() {
            return ward;
        }

        public void setWard(String ward) {
            this.ward = ward;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserProfileData {
        private String userId;
        private String username;
        private String fullName;
        private String email;
        private String phoneNumber;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        private String userType;

        // Dành riêng cho Khách hàng (CUSTOMER)
        private List<AddressData> addresses;

        // Dành riêng cho Nhân viên (EMPLOYEE)
        private String position;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate hireDate;

        public UserProfileData() {
        }

        // Constructor cho Khách hàng (CUSTOMER)
        public UserProfileData(String userId, String username, String fullName, String email, String phoneNumber,
                LocalDate birthDate, String userType, List<AddressData> addresses) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.birthDate = birthDate;
            this.userType = userType;
            this.addresses = addresses;
        }

        // Constructor cho Nhân viên (EMPLOYEE)
        public UserProfileData(String userId, String username, String fullName, String email, String phoneNumber,
                LocalDate birthDate, String userType, String position, LocalDate hireDate) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.birthDate = birthDate;
            this.userType = userType;
            this.position = position;
            this.hireDate = hireDate;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public List<AddressData> getAddresses() {
            return addresses;
        }

        public void setAddresses(List<AddressData> addresses) {
            this.addresses = addresses;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public LocalDate getHireDate() {
            return hireDate;
        }

        public void setHireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
        }
    }
}
