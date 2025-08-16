package com.blobstorage.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L; // Add this line to fix the warning

    private int statusCode;
    private String message;

    public CustomException(String message, int statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getJsonResponse() {
        return "{ \"error\": \"" + message + "\", \"status\": " + statusCode + " }";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
