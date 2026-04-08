package org.ac.cst8277.baimakov.dmitriy.dto;

public record ApiResponse<T>(String code, T data, String message) {
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>("200", data, message);
    }
    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>("201", data, message);
    }
    public static <T> ApiResponse<T> error(String code, T data, String message) {
        return new ApiResponse<>(code, data, message);
    }
}
