package com.proyecto.servicios.model.clientes;

import lombok.Data;

@Data
public class CatalogoResponseDTO {
    private Long id;
    private String tipo;
    private String clave;
    private String descripcion;
}
