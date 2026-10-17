package com.proyecto.servicios.model.clientes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Petición para operaciones de reactivación o baja de cliente mediante ID o CURP")
public class ClienteIdRequestDTO {

    @Positive(message = "El ID del cliente debe ser un número positivo")
    @Schema(description = "ID del cliente", example = "1")
    private Long id;

    @Pattern(regexp = "^$|^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "El formato de la CURP es inválido")
    @Schema(description = "CURP del cliente (opcional si se envía ID)", example = "JUAN800101HDFRRN01")
    private String curp;
}
