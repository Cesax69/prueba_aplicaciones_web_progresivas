package com.proyecto.servicios.exception;

public class CurpDuplicadaException extends ClienteException {
    public CurpDuplicadaException(String message) {
        super(message, 409);
    }
}
