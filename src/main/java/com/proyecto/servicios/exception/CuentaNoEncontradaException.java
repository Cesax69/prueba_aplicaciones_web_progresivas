package com.proyecto.servicios.exception;

public class CuentaNoEncontradaException extends ClienteException {
    public CuentaNoEncontradaException(String message) {
        super(message, 404);
    }
}
