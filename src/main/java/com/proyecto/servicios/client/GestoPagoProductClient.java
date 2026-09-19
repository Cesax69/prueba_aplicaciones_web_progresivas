package com.proyecto.servicios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente Feign para consumir el endpoint de productos de GestoPago.
 * Retorna la respuesta como String para manejar tanto XML como JSON,
 * ya que el servicio externo puede responder en text/xml.
 */
@FeignClient(name = "gestoPagoProductClient", url = "${gestopago.api.url}")
public interface GestoPagoProductClient {

    @GetMapping(value = "/sistema/service/getProductList.do", produces = "*/*")
    String getProductList(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-API-Key") String apiKey
    );
}
