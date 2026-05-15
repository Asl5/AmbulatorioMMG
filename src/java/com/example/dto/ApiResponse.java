package com.example.dto;

public class ApiResponse<T> {
    public String esito;
    public String message;
    public T data;

    public ApiResponse(String esito, String message, T data) {
        this.esito = esito;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("ok", null, data);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>("ok", message, data);
    }

    public static <T> ApiResponse<T> ko(String message) {
        return new ApiResponse<>("ko", message, null);
    }
}
