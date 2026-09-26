package com.proyecto.servicios.model.clientes;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CuentaResponseDTO {
    private String numeroCuenta;
    private String estatus;
    private BigDecimal saldo;
    private LocalDateTime fechaApertura;
    private Long clienteId;
    private String nombreCliente;
}
