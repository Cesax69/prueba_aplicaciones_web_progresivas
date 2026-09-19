package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa un grupo de productos clasificados por su {@code tipoFront}.
 * <p>
 * El campo {@code tipoFront} categoriza el tipo de interfaz que debe usar
 * el cliente para presentar el producto al usuario final.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductGroupDTO {

    /** Valor del tipoFront que agrupa estos productos */
    private String tipoFront;

    /** Cantidad total de productos en este grupo */
    private int total;

    /** Lista de productos que pertenecen a este tipoFront */
    private List<ProductDTO> productos;
}
