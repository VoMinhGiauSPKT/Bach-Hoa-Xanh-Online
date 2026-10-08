package com.mycompany.bachhoaxanhonline.module.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

public class EmployeeResponse {

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
    public static class EmployeeData {
        private String employeeId;
        private String fullName;
        private String username;
        private String email;
        private String phoneNumber;
        private String position;
        private boolean status;

        public EmployeeData() {
        }

        public EmployeeData(String employeeId, String fullName, String username, String phoneNumber, String position,
                boolean status) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.username = username;
            this.phoneNumber = phoneNumber;
            this.position = position;
            this.status = status;
        }

        public EmployeeData(String employeeId, String fullName, String username, String email, String phoneNumber,
                String position, boolean status) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.username = username;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.position = position;
            this.status = status;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmployeeDetailData {
        private String employeeId;
        private String fullName;
        private String username;
        private String email;
        private String phoneNumber;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        private String position;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate hireDate;

        private boolean status;

        public EmployeeDetailData() {
        }

        public EmployeeDetailData(String employeeId, String fullName, String username, String email, String phoneNumber,
                LocalDate birthDate, String position, LocalDate hireDate, boolean status) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.username = username;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.birthDate = birthDate;
            this.position = position;
            this.hireDate = hireDate;
            this.status = status;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmployeeListItem {
        private String employeeId;
        private String fullName;
        private String username;
        private String email;
        private String phoneNumber;
        private String position;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate hireDate;

        private boolean status;

        public EmployeeListItem() {
        }

        public EmployeeListItem(String employeeId, String fullName, String username, String email, String phoneNumber,
                String position, LocalDate hireDate, boolean status) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.username = username;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.position = position;
            this.hireDate = hireDate;
            this.status = status;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmployeeListData {
        private long total;
        private int page;
        private int limit;
        private List<EmployeeListItem> employees;

        public EmployeeListData() {
        }

        public EmployeeListData(long total, int page, int limit, List<EmployeeListItem> employees) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.employees = employees;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public List<EmployeeListItem> getEmployees() {
            return employees;
        }

        public void setEmployees(List<EmployeeListItem> employees) {
            this.employees = employees;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateStatusData {
        private String employeeId;
        private boolean status;

        public UpdateStatusData() {
        }

        public UpdateStatusData(String employeeId, boolean status) {
            this.employeeId = employeeId;
            this.status = status;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }
}
