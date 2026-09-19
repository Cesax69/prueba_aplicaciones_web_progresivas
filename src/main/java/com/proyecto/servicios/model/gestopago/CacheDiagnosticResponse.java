package com.proyecto.servicios.model.gestopago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheDiagnosticResponse {
    private int codigo; // 0 exito, 1 error en redis, 2 error en bd
    private String origen; // REDIS, BASE_DE_DATOS, NINGUNO
    private String mensaje;
    private ProductGroupedResponse datos;
}
