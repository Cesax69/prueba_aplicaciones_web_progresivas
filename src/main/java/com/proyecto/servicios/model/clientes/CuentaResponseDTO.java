package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CuentaResponseDTO {
    private String numeroCuenta;
    private String estatus;
    private BigDecimal saldo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaApertura;

    private Long clienteId;
    private String nombreCliente;
}
