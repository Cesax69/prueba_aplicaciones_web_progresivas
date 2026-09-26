package com.proyecto.servicios.exception;

public class ClienteException extends RuntimeException {
    private final int statusCode;

    public ClienteException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ClienteException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }
}
