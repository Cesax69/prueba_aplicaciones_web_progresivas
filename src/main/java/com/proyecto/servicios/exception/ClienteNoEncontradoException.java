package com.proyecto.servicios.exception;

public class ClienteNoEncontradoException extends ClienteException {
    public ClienteNoEncontradoException(String message) {
        super(message, 404);
    }
}
