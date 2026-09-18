package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoClient;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.service.GestoPagoService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GestoPagoServiceImpl implements GestoPagoService {

    private final GestoPagoClient gestoPagoClient;

    public GestoPagoServiceImpl(GestoPagoClient gestoPagoClient) {
        this.gestoPagoClient = gestoPagoClient;
    }

    @Override
    public ProductListResponse obtenerListaProductos() {
        log.info("Iniciando invocación al servicio externo de GestoPago para obtener la lista de productos");
        try {
            ProductListResponse response = gestoPagoClient.getProductList();
            
            // Validar si la respuesta es exitosa desde el punto de vista del negocio (opcional, dependiendo de la API)
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
            // Re-lanzamos nuestra propia excepción para no envolverla nuevamente en un Exception genérico
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al intentar obtener la lista de productos de GestoPago", e);
            throw new GestoPagoException("Error interno al procesar la integración con GestoPago", e, 500);
        } finally {
            log.info("Fin de la invocación al servicio externo de GestoPago");
        }
    }
}
