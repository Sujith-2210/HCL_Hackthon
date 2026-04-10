package com.hotel.dto.response;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}
    private ApiResponse(boolean success, String message, T data) {
        this.success = success; this.message = message; this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // For validation errors builder pattern
    public static <T> Builder<T> builder() { return new Builder<>(); }
    public static class Builder<T> {
        private boolean success; private String message; private T data;
        public Builder<T> success(boolean v) { this.success = v; return this; }
        public Builder<T> message(String v) { this.message = v; return this; }
        public Builder<T> data(T v) { this.data = v; return this; }
        public ApiResponse<T> build() { return new ApiResponse<>(success, message, data); }
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
