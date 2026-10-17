package com.proyecto.servicios.model.clientes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de respuesta para operaciones de clientes.
 * Hereda todos los campos de ClienteResponseDTO y GenericResponse (codigo, mensaje).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Respuesta unificada de cliente")
public class ClienteResponse extends ClienteResponseDTO {
}
