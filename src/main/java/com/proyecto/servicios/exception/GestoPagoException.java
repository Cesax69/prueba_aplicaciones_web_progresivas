package com.proyecto.servicios.exception;

public class GestoPagoException extends RuntimeException {

    private final int statusCode;

    public GestoPagoException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public GestoPagoException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
