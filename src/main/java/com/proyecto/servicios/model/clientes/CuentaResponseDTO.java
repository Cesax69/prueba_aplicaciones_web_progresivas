package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.proyecto.servicios.model.GenericResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CuentaResponseDTO extends GenericResponse {
    private String numeroCuenta;
    private String estatus;
    private BigDecimal saldo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaApertura;

    private String nombreCliente;
}
