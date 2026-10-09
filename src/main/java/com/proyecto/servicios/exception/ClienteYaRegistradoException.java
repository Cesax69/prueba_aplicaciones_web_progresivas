package com.proyecto.servicios.exception;

public class ClienteYaRegistradoException extends ClienteException {
    public ClienteYaRegistradoException(String message) {
        super(message, 409);
    }
}
