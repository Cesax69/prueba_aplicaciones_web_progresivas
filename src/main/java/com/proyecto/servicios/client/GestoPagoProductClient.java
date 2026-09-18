package com.proyecto.servicios.client;

import com.proyecto.servicios.model.gestopago.ProductListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gestoPagoProductClient", url = "${gestopago.api.url}")
public interface GestoPagoProductClient {

    @GetMapping("/sistema/service/getProductList.do")
    ProductListResponse getProductList(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-API-Key") String apiKey
    );
}
