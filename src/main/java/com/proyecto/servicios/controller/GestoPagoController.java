package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.service.GestoPagoProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestopago")
@Tag(name = "GestoPago", description = "API para integración con servicios de GestoPago / PuntoRed")
@Slf4j
public class GestoPagoController {

    private final GestoPagoProductService gestoPagoService;

    public GestoPagoController(GestoPagoProductService gestoPagoService) {
        this.gestoPagoService = gestoPagoService;
    }

    @GetMapping("/productos")
    @Operation(summary = "Obtener lista de productos", description = "Consulta el servicio externo de GestoPago para obtener el catálogo de productos disponibles.")
    public ResponseEntity<ProductListResponse> obtenerListaProductos() {
        log.info("REST request para obtener lista de productos de GestoPago");
        ProductListResponse response = gestoPagoService.obtenerListaProductos();
        return ResponseEntity.ok(response);
    }
}
