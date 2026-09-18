package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.ProductListResponse;

public interface GestoPagoService {
    
    /**
     * Obtiene la lista de productos desde el servicio externo de GestoPago.
     * 
     * @return ProductListResponse respuesta con la lista de productos
     */
    ProductListResponse obtenerListaProductos();
}
