package com.proyecto.servicios.exception;

public class ValidacionException extends ClienteException {
    public ValidacionException(String message) {
        super(message, 400);
    }
}
