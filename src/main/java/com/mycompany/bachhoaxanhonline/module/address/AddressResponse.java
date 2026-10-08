package com.mycompany.bachhoaxanhonline.module.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressResponse {

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

        public ApiResponse(int status, T data) {
            this.status = status;
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

        @JsonProperty("isDefault")
        private Boolean isDefault;

        public AddressData() {
        }

        public AddressData(Long addressId, String receiverName, String phoneNumber,
                           String street, String ward, String city, Boolean isDefault) {
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
    public static class DefaultAddressData {
        private Long addressId;

        @JsonProperty("isDefault")
        private Boolean isDefault;

        public DefaultAddressData() {
        }

        public DefaultAddressData(Long addressId, Boolean isDefault) {
            this.addressId = addressId;
            this.isDefault = isDefault;
        }

        public Long getAddressId() {
            return addressId;
        }

        public void setAddressId(Long addressId) {
            this.addressId = addressId;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }
    }
}
