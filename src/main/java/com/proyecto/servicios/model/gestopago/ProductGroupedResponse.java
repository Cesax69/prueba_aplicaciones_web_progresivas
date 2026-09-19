package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta de la API con productos agrupados por {@code tipoFront}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductGroupedResponse {

    private String status;

    private String message;

    /** Total de productos obtenidos del servicio externo */
    private int totalProductos;

    /** Total de grupos (distintos tipoFront) */
    private int totalGrupos;

    /** Productos agrupados por tipoFront */
    private List<ProductGroupDTO> grupos;
}
