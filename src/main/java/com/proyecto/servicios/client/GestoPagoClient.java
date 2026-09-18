package com.proyecto.servicios.client;

import com.proyecto.servicios.model.gestopago.ProductListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "gestoPagoClient", url = "${gestopago.api.url}", configuration = GestoPagoClientConfig.class)
public interface GestoPagoClient {

    @GetMapping("/sistema/service/getProductList.do")
    ProductListResponse getProductList();
}
