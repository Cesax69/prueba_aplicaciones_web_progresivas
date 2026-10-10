package com.proyecto.servicios.model.clientes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClienteCurpRequestDTO {

    @NotBlank(message = "La CURP no puede estar vacía")
    @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "El formato de la CURP es inválido. Solo se aceptan 18 caracteres alfanuméricos en mayúscula")
    private String curp;
}
