package com.proyecto.servicios.model.clientes;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRangoFechasRequestDTO {

    @NotNull(message = "El parámetro 'inicio' es requerido")
    private LocalDate inicio;

    @NotNull(message = "El parámetro 'fin' es requerido")
    private LocalDate fin;
}
