package com.proyecto.servicios.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleClienteNoEncontrado(ClienteNoEncontradoException ex) {
        log.error("Cliente no encontrado", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CurpDuplicadaException.class)
    public ResponseEntity<ErrorResponse> handleCurpDuplicada(CurpDuplicadaException ex) {
        log.error("CURP duplicada", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(RfcDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRfcDuplicado(RfcDuplicadoException ex) {
        log.error("RFC duplicado", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleCorreoDuplicado(CorreoDuplicadoException ex) {
        log.error("Correo duplicado", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(ClienteYaRegistradoException.class)
    public ResponseEntity<ErrorResponse> handleClienteYaRegistrado(ClienteYaRegistradoException ex) {
        log.error("Cliente ya registrado", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        log.error("Cuenta no encontrada", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(ValidacionException ex) {
        log.error("Error de validación", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(ClienteException.class)
    public ResponseEntity<ErrorResponse> handleClienteException(ClienteException ex) {
        log.error("Error en módulo de clientes", ex);
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        log.error("Error de validación de campos: {}", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.error("Error de formato en JSON", ex);

        String mensajeEspecifico = "Error en el formato del JSON. Verifique que los tipos de datos sean correctos.";

        Throwable causa = ex.getCause();
        if (causa instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mismatch) {
            String campo = mismatch.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .filter(f -> f != null)
                    .reduce((a, b) -> a + "." + b)
                    .orElse(null);

            String tipoCampo = mismatch.getTargetType() != null ? mismatch.getTargetType().getSimpleName() : "desconocido";

            if (campo != null) {
                if (tipoCampo.equals("BigDecimal") || tipoCampo.equals("Double") || tipoCampo.equals("Float")
                        || tipoCampo.equals("Long") || tipoCampo.equals("Integer")) {
                    mensajeEspecifico = "El campo '" + campo + "' debe ser un número sin comillas (ej: " + campo + ": 1234.56). No envíe el valor entre comillas.";
                } else if (tipoCampo.equals("Long") || tipoCampo.equals("Integer")) {
                    mensajeEspecifico = "El campo '" + campo + "' debe ser un número entero sin comillas (ej: " + campo + ": 15). No envíe el valor entre comillas.";
                } else {
                    mensajeEspecifico = "El campo '" + campo + "' tiene un tipo de dato incorrecto. Se esperaba: " + tipoCampo + ".";
                }
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, mensajeEspecifico, LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Error interno del servidor", LocalDateTime.now()));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String mensaje;
        private LocalDateTime timestamp;
    }
}
