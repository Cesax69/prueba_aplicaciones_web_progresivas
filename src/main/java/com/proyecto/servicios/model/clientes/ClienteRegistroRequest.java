package com.proyecto.servicios.model.clientes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO para registro y actualización completa de clientes.
 * Hereda todas las validaciones y campos de ClienteRequestDTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Petición de registro y actualización completa de cliente")
public class ClienteRegistroRequest extends ClienteRequestDTO {
}
