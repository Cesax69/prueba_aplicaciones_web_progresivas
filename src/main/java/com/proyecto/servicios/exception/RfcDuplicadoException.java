package com.proyecto.servicios.exception;

public class RfcDuplicadoException extends ClienteException {
    public RfcDuplicadoException(String message) {
        super(message, 409);
    }
}
