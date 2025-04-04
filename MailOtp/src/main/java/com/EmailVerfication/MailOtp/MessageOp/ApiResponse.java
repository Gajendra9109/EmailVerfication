package com.EmailVerfication.MailOtp.MessageOp;

public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    // Constructor
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    // Override toString to output properly formatted response
    @Override
    public String toString() {
        return "{ \"status\": \"" + status + "\", " +
               "\"message\": \"" + message + "\", " +
               "\"data\": \"" + data + "\" }";
    }
}
