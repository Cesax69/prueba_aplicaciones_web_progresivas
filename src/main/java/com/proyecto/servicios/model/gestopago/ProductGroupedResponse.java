package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.proyecto.servicios.enums.DataSourceEnum;
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

    private Integer codigoCache;

    private String origenCache;

    /** Total de productos obtenidos del servicio externo */
    private int totalProductos;

    /** Total de grupos (distintos tipoFront) */
    private int totalGrupos;

    /** Productos agrupados por tipoFront */
    private List<ProductGroupDTO> grupos;

    public void setOrigenDatos(DataSourceEnum origen) {
        if (origen != null) {
            this.codigoCache = origen.getCodigo();
            this.origenCache = origen.getDescripcion();
        }
    }
}
