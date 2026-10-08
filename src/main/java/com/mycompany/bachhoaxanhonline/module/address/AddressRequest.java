package com.mycompany.bachhoaxanhonline.module.address;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressRequest {

    public static class CreateAddressRequest {
        private String receiverName;
        private String phoneNumber;
        private String street;
        private String ward;
        private String city;

        @JsonProperty("isDefault")
        private Boolean isDefault;

        public CreateAddressRequest() {
        }

        public CreateAddressRequest(String receiverName, String phoneNumber, String street,
                                    String ward, String city, Boolean isDefault) {
            this.receiverName = receiverName;
            this.phoneNumber = phoneNumber;
            this.street = street;
            this.ward = ward;
            this.city = city;
            this.isDefault = isDefault;
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

    public static class UpdateAddressRequest {
        private String receiverName;
        private String phoneNumber;
        private String street;
        private String ward;
        private String city;

        @JsonProperty("isDefault")
        private Boolean isDefault;

        public UpdateAddressRequest() {
        }

        public UpdateAddressRequest(String receiverName, String phoneNumber, String street,
                                    String ward, String city, Boolean isDefault) {
            this.receiverName = receiverName;
            this.phoneNumber = phoneNumber;
            this.street = street;
            this.ward = ward;
            this.city = city;
            this.isDefault = isDefault;
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
}
