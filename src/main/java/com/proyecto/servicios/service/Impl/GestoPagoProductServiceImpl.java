package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoProductCache;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductCacheRepository;
import com.proyecto.servicios.service.GestoPagoProductService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementación del servicio de productos de GestoPago.
 * <p>
 * Estrategia de caché:
 * <ul>
 *   <li>Respuesta 200 → se guarda en Redis (TTL configurable), no en BD.</li>
 *   <li>Si Redis falla → fallback: se guarda en la tabla {@code gestopago_product_cache} de la BD.</li>
 *   <li>Cron diario a las 06:00 AM → refresca la caché automáticamente.</li>
 * </ul>
 * <p>
 * El servicio externo puede responder en XML o JSON. Se detecta automáticamente.
 */
@Service
@Slf4j
public class GestoPagoProductServiceImpl implements GestoPagoProductService {

    private static final String REDIS_KEY = "gestopago:productos";

    private final GestoPagoProductClient gestoPagoProductClient;
    private final GestoPagoTokenService tokenService;
    private final RedisTemplate<String, String> redisTemplate;
    private final GestoPagoProductCacheRepository productCacheRepository;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    @Value("${gestopago.auth.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo}")
    private String codigoDispositivo;

    @Value("${gestopago.api.key}")
    private String apiKey;

    @Value("${gestopago.cache.ttl-horas:1}")
    private long cacheTtlHoras;

    public GestoPagoProductServiceImpl(GestoPagoProductClient gestoPagoProductClient,
                                       GestoPagoTokenService tokenService,
                                       RedisTemplate<String, String> redisTemplate,
                                       GestoPagoProductCacheRepository productCacheRepository,
                                       ObjectMapper objectMapper) {
        this.gestoPagoProductClient = gestoPagoProductClient;
        this.tokenService = tokenService;
        this.redisTemplate = redisTemplate;
        this.productCacheRepository = productCacheRepository;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
    }

    /**
     * Obtiene la lista de productos. Intenta servir desde Redis primero;
     * si no hay caché, consulta a GestoPago y guarda el resultado.
     */
    @Override
    public ProductListResponse obtenerListaProductos() {
        log.info("Iniciando obtención de lista de productos GestoPago");
        try {
            // 1. Intentar servir desde Redis
            ProductListResponse desdeRedis = obtenerDesdeRedis();
            if (desdeRedis != null) {
                log.info("Productos obtenidos desde caché Redis");
                return desdeRedis;
            }

            // 2. Redis vacío o expirado → consultar al servicio externo
            ProductListResponse response = consultarServicioExterno();

            // 3. Guardar resultado en caché
            guardarEnCache(response);

            return response;

        } finally {
            log.info("Fin de la invocación al servicio de productos GestoPago");
        }
    }

    /**
     * Cron que se ejecuta todos los días a las 06:00 AM para refrescar la caché de productos.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void refrescarCacheProductos() {
        log.info("Cron 06:00 AM - Iniciando refresco automático de caché de productos GestoPago");
        try {
            ProductListResponse response = consultarServicioExterno();
            guardarEnCache(response);
            log.info("Cron 06:00 AM - Caché de productos refrescada correctamente");
        } catch (Exception e) {
            log.error("Cron 06:00 AM - Error al refrescar caché de productos: {}", e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Métodos privados de apoyo
    // -------------------------------------------------------------------------

    /**
     * Intenta leer la lista de productos desde Redis.
     *
     * @return {@link ProductListResponse} deserializado o {@code null} si no hay datos.
     */
    private ProductListResponse obtenerDesdeRedis() {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_KEY);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, ProductListResponse.class);
            }
        } catch (Exception e) {
            log.warn("No se pudo leer el caché de Redis (clave: {}): {}", REDIS_KEY, e.getMessage());
        }
        return null;
    }

    /**
     * Llama al cliente Feign para obtener los productos directamente del servicio externo.
     * Detecta automáticamente si la respuesta es XML o JSON y la parsea correctamente.
     *
     * @return {@link ProductListResponse} con los datos de GestoPago.
     * @throws GestoPagoException si hay un error de comunicación o la respuesta es errónea.
     */
    private ProductListResponse consultarServicioExterno() {
        log.info("Consultando lista de productos al servicio externo de GestoPago");
        try {
            // Obtener el token activo de la BD dinámicamente
            Optional<GestoPagoToken> tokenOpt = tokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo);
            String bearerToken = tokenOpt.map(t -> "Bearer " + t.getToken()).orElse("");

            if (bearerToken.isEmpty()) {
                log.warn("No se encontró un token activo en la base de datos para GestoPago.");
            }

            // Llamar al servicio externo (respuesta en crudo: puede ser XML o JSON)
            String rawResponse = gestoPagoProductClient.getProductList(bearerToken, apiKey);
            log.debug("Respuesta cruda recibida del servicio externo (primeros 300 chars): {}",
                    rawResponse != null ? rawResponse.substring(0, Math.min(rawResponse.length(), 300)) : "null");

            // Parsear la respuesta según su formato
            ProductListResponse response = parsearRespuesta(rawResponse);

            // Para respuestas XML el estado se calcula desde los datos recibidos
            if (response != null && (response.getStatus() == null || response.getStatus().isBlank())) {
                response.calcularStatus();
            }

            if (response == null || "ERROR".equalsIgnoreCase(response.getStatus())) {
                String errorMsg = response != null ? response.getMessage() : "Respuesta nula o inválida";
                log.error("El servicio externo respondió con error lógico: {}", errorMsg);
                throw new GestoPagoException("Error en la respuesta del servicio externo: " + errorMsg, 500);
            }

            log.info("Invocación exitosa al servicio externo, productos recibidos correctamente");
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
            log.error("Error inesperado al obtener la lista de productos de GestoPago", e);
            throw new GestoPagoException("Error interno al procesar la integración con GestoPago", e, 500);
        }
    }

    /**
     * Detecta si la respuesta es XML o JSON y la parsea al objeto {@link ProductListResponse}.
     *
     * @param rawResponse Respuesta en crudo del servicio externo.
     * @return {@link ProductListResponse} parseado.
     */
    private ProductListResponse parsearRespuesta(String rawResponse) throws Exception {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }
        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("<")) {
            // Respuesta en formato XML
            log.debug("Respuesta detectada como XML, parseando con XmlMapper");
            return xmlMapper.readValue(trimmed, ProductListResponse.class);
        } else {
            // Respuesta en formato JSON
            log.debug("Respuesta detectada como JSON, parseando con ObjectMapper");
            return objectMapper.readValue(trimmed, ProductListResponse.class);
        }
    }

    /**
     * Guarda el resultado en Redis. Si Redis falla, guarda en la BD como fallback.
     *
     * @param response La respuesta exitosa del servicio externo.
     */
    private void guardarEnCache(ProductListResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(REDIS_KEY, json, cacheTtlHoras, TimeUnit.HOURS);
            log.info("Lista de productos guardada en Redis (TTL: {} hora(s))", cacheTtlHoras);
        } catch (Exception redisEx) {
            log.warn("Redis no disponible. Guardando caché de productos en BD como fallback: {}", redisEx.getMessage());
            guardarEnBd(response);
        }
    }

    /**
     * Fallback: serializa y persiste la lista de productos en la tabla
     * {@code gestopago_product_cache} de la BD.
     *
     * @param response La respuesta a persistir.
     */
    private void guardarEnBd(ProductListResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            GestoPagoProductCache cache = productCacheRepository
                    .findTopByOrderByFechaActualizacionDesc()
                    .orElseGet(GestoPagoProductCache::new);
            cache.setProductosJson(json);
            productCacheRepository.save(cache);
            log.info("Caché de productos guardada correctamente en BD (fallback)");
        } catch (JsonProcessingException e) {
            log.error("Error al serializar la lista de productos para guardar en BD: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error al guardar caché de productos en BD: {}", e.getMessage(), e);
        }
    }
}