package com.mycompany.bachhoaxanhonline.module.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PaymentResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentLinkData {
        private String orderId;
        private Long orderCode;
        private String checkoutUrl;
        private String qrCode;
        private String bin;
        private String accountNumber;
        private String accountName;
        private Integer amount;
        private String status;

        public PaymentLinkData() {
        }

        public PaymentLinkData(String orderId, Long orderCode, String checkoutUrl, String qrCode,
                               String bin, String accountNumber, String accountName, Integer amount, String status) {
            this.orderId = orderId;
            this.orderCode = orderCode;
            this.checkoutUrl = checkoutUrl;
            this.qrCode = qrCode;
            this.bin = bin;
            this.accountNumber = accountNumber;
            this.accountName = accountName;
            this.amount = amount;
            this.status = status;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Long getOrderCode() {
            return orderCode;
        }

        public void setOrderCode(Long orderCode) {
            this.orderCode = orderCode;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public void setCheckoutUrl(String checkoutUrl) {
            this.checkoutUrl = checkoutUrl;
        }

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }

        public String getBin() {
            return bin;
        }

        public void setBin(String bin) {
            this.bin = bin;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
