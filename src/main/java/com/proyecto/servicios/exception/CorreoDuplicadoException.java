package com.proyecto.servicios.exception;

public class CorreoDuplicadoException extends ClienteException {
    public CorreoDuplicadoException(String message) {
        super(message, 409);
    }
}
