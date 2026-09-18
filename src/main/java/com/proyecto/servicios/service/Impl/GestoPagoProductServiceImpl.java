package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.service.GestoPagoProductService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class GestoPagoProductServiceImpl implements GestoPagoProductService {

    private final GestoPagoProductClient gestoPagoProductClient;
    private final GestoPagoTokenService tokenService;

    @Value("${gestopago.auth.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo}")
    private String codigoDispositivo;

    @Value("${gestopago.api.key}")
    private String apiKey;

    public GestoPagoProductServiceImpl(GestoPagoProductClient gestoPagoProductClient, GestoPagoTokenService tokenService) {
        this.gestoPagoProductClient = gestoPagoProductClient;
        this.tokenService = tokenService;
    }

    @Override
    public ProductListResponse obtenerListaProductos() {
        log.info("Iniciando invocación al servicio externo de GestoPago para obtener la lista de productos");
        try {
            // 1. Obtener el token de la BD dinámicamente
            Optional<GestoPagoToken> tokenOpt = tokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo);
            String bearerToken = tokenOpt.map(t -> "Bearer " + t.getToken()).orElse("");
            
            if (bearerToken.isEmpty()) {
                log.warn("No se encontró un token activo en la base de datos para GestoPago.");
            }

            // 2. Realizar la petición
            ProductListResponse response = gestoPagoProductClient.getProductList(bearerToken, apiKey);
            
            // Validar si la respuesta es exitosa desde el punto de vista del negocio
            if (response == null || "ERROR".equalsIgnoreCase(response.getStatus())) {
                String errorMsg = response != null ? response.getMessage() : "Respuesta nula";
                log.error("El servicio externo respondió con un error lógico: {}", errorMsg);
                throw new GestoPagoException("Error en la respuesta del servicio externo: " + errorMsg, 500);
            }
            
            log.info("Invocación exitosa, se obtuvieron los productos correctamente");
            return response;
            
        } catch (FeignException.Unauthorized e) {
            log.error("Error de autenticación al consumir el servicio externo: Unauthorized (401)", e);
            throw new GestoPagoException("Error de autenticación con el servicio externo. Verifique las credenciales/token.", e, 401);
        } catch (FeignException.GatewayTimeout | FeignException.ServiceUnavailable e) {
            log.error("Timeout o servicio no disponible al intentar conectar con GestoPago", e);
            throw new GestoPagoException("Timeout al conectar con el servicio externo.", e, 504);
        } catch (FeignException e) {
            log.error("Error de comunicación HTTP ({}) al consumir el servicio externo", e.status(), e);
            throw new GestoPagoException("Error HTTP al comunicarse con el servicio externo", e, e.status());
        } catch (GestoPagoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al intentar obtener la lista de productos de GestoPago", e);
            throw new GestoPagoException("Error interno al procesar la integración con GestoPago", e, 500);
        } finally {
            log.info("Fin de la invocación al servicio externo de GestoPago");
        }
    }
}
