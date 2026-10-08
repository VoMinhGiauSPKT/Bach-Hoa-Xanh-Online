package com.mycompany.bachhoaxanhonline.common;

import java.io.Serializable;

/**
 * Lớp vỏ bọc chuẩn (Response Envelope) cho toàn bộ REST API của hệ thống Bách Hóa Xanh Online.
 * Chuẩn hóa cấu trúc đầu ra: status, message, data.
 */
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

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

    // ================= STATIC FACTORY HELPERS =================

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "Thành công", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    // ================= GETTERS & SETTERS =================

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
