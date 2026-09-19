package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.ProductGroupedResponse;
import com.proyecto.servicios.model.gestopago.ProductListResponse;

public interface GestoPagoProductService {

    /**
     * Obtiene la lista de productos desde el servicio externo de GestoPago (lista plana).
     *
     * @return ProductListResponse respuesta con la lista completa de productos
     */
    ProductListResponse obtenerListaProductos();

    /**
     * Obtiene los productos desde el servicio externo de GestoPago agrupados por tipoFront.
     *
     * @return ProductGroupedResponse respuesta con productos organizados por tipoFront
     */
    ProductGroupedResponse obtenerProductosAgrupados();
}
