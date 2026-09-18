package com.proyecto.servicios.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class GestoPagoClientConfig {

    @Value("${gestopago.api.token}")
    private String apiToken;

    @Value("${gestopago.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.debug("Agregando cabeceras de autenticación a la petición Feign");
                
                if (apiToken != null && !apiToken.isEmpty()) {
                    template.header("Authorization", "Bearer " + apiToken);
                } else {
                    log.warn("El token Bearer no está configurado (gestopago.api.token)");
                }

                if (apiKey != null && !apiKey.isEmpty()) {
                    template.header("X-API-Key", apiKey);
                }
            }
        };
    }
}
