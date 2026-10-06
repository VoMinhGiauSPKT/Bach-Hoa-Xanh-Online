package com.mycompany.bachhoaxanhonline.module.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

public class AuthResponse {

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

    public static class RegisterData {
        private String customerId;
        private String username;
        private String fullName;
        private String email;
        private String phoneNumber;

        public RegisterData() {
        }

        public RegisterData(String customerId, String username, String fullName, String email, String phoneNumber) {
            this.customerId = customerId;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
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
    }

    public static class EmployeeInfo {
        private String employeeId;
        private String username;
        private String fullName;
        private String position;

        public EmployeeInfo() {
        }

        public EmployeeInfo(String employeeId, String username, String fullName, String position) {
            this.employeeId = employeeId;
            this.username = username;
            this.fullName = fullName;
            this.position = position;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
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

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static class CustomerInfo {
        private String customerId;
        private String username;
        private String fullName;

        public CustomerInfo() {
        }

        public CustomerInfo(String customerId, String username, String fullName) {
            this.customerId = customerId;
            this.username = username;
            this.fullName = fullName;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
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
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginData {
        private String accessToken;
        private String userType;
        private EmployeeInfo employee;
        private CustomerInfo customer;

        public LoginData() {
        }

        // Constructor for Employee
        public static LoginData forEmployee(String accessToken, EmployeeInfo employee) {
            LoginData data = new LoginData();
            data.accessToken = accessToken;
            data.userType = "EMPLOYEE";
            data.employee = employee;
            return data;
        }

        // Constructor for Customer
        public static LoginData forCustomer(String accessToken, CustomerInfo customer) {
            LoginData data = new LoginData();
            data.accessToken = accessToken;
            data.userType = "CUSTOMER";
            data.customer = customer;
            return data;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public EmployeeInfo getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeInfo employee) {
            this.employee = employee;
        }

        public CustomerInfo getCustomer() {
            return customer;
        }

        public void setCustomer(CustomerInfo customer) {
            this.customer = customer;
        }
    }

    public static class TokenData {
        private String accessToken;

        public TokenData() {
        }

        public TokenData(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
