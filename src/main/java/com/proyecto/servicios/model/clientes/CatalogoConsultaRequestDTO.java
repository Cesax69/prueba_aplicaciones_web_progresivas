package com.proyecto.servicios.model.clientes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Petición para consultar elementos de catálogo por tipo sin usar PathVariable")
public class CatalogoConsultaRequestDTO {

    @NotBlank(message = "El tipo de catálogo es obligatorio (ej: NACIONALIDAD, SEXO, ESTADO_CIVIL, OCUPACION)")
    @Schema(description = "Tipo de catálogo", example = "NACIONALIDAD")
    private String tipo;
}
