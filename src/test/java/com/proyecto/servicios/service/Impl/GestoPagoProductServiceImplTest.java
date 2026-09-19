package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoProductCache;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.model.gestopago.ProductDTO;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductCacheRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductServiceImplTest {

    @Mock
    private GestoPagoProductClient gestoPagoProductClient;

    @Mock
    private GestoPagoTokenService tokenService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private GestoPagoProductCacheRepository productCacheRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GestoPagoProductServiceImpl gestoPagoService;

    private Request request;
    private ProductListResponse mockResponse;
    private static final String JSON_RESPONSE = "{\"status\":\"OK\",\"message\":null,\"data\":[{\"id\":\"1\",\"name\":\"Producto 1\"}]}";

    @BeforeEach
    void setUp() {
        request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, new RequestTemplate());

        ProductDTO product = new ProductDTO("1", "Producto 1", "Servicio 1", "100", "10.0", "a", "1", "false", "11");
        mockResponse = new ProductListResponse();
        mockResponse.setStatus("OK");
        mockResponse.setData(List.of(product));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // -------------------------------------------------------------------------
    // Escenario 1: Sirve desde Redis cuando hay datos en caché
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_desdeRedis_exitoso() throws Exception {
        // Arrange
        when(valueOperations.get("gestopago:productos")).thenReturn(JSON_RESPONSE);
        when(objectMapper.readValue(JSON_RESPONSE, ProductListResponse.class)).thenReturn(mockResponse);

        // Act
        ProductListResponse result = gestoPagoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        // No debe llamar al cliente externo
        verifyNoInteractions(gestoPagoProductClient);
    }

    // -------------------------------------------------------------------------
    // Escenario 2: Redis vacío → llama al servicio externo (respuesta JSON) y guarda en Redis
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_redisVacio_consultaExternoJsonYGuardaEnRedis() throws Exception {
        // Arrange
        when(valueOperations.get("gestopago:productos")).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenReturn(JSON_RESPONSE);
        when(objectMapper.readValue(JSON_RESPONSE, ProductListResponse.class)).thenReturn(mockResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn(JSON_RESPONSE);

        // Act
        ProductListResponse result = gestoPagoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        // Debe guardar en Redis
        verify(valueOperations, times(1)).set(eq("gestopago:productos"), anyString(), anyLong(), eq(TimeUnit.HOURS));
        // No debe guardar en BD (Redis funcionó)
        verifyNoInteractions(productCacheRepository);
    }

    // -------------------------------------------------------------------------
    // Escenario 3: Redis falla → guarda en BD como fallback
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_redisFalla_guardaEnBd() throws Exception {
        // Arrange
        when(valueOperations.get("gestopago:productos")).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenReturn(JSON_RESPONSE);
        when(objectMapper.readValue(JSON_RESPONSE, ProductListResponse.class)).thenReturn(mockResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn(JSON_RESPONSE);
        // Simular que Redis lanza excepción al escribir
        doThrow(new RuntimeException("Redis connection refused"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        when(productCacheRepository.findTopByOrderByFechaActualizacionDesc())
                .thenReturn(Optional.of(new GestoPagoProductCache()));

        // Act
        ProductListResponse result = gestoPagoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        // Debe haber guardado en BD
        verify(productCacheRepository, times(1)).save(any(GestoPagoProductCache.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 4: Servicio externo responde Unauthorized (401)
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_unauthorized() {
        // Arrange
        FeignException.Unauthorized unauthorized = new FeignException.Unauthorized(
                "Unauthorized", request, null, new HashMap<>());
        when(valueOperations.get("gestopago:productos")).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenThrow(unauthorized);

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class,
                () -> gestoPagoService.obtenerListaProductos());

        assertEquals(401, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Error de autenticación"));
    }

    // -------------------------------------------------------------------------
    // Escenario 5: Timeout del servicio externo (504)
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_timeout() {
        // Arrange
        FeignException.GatewayTimeout timeout = new FeignException.GatewayTimeout(
                "Gateway Timeout", request, null, new HashMap<>());
        when(valueOperations.get("gestopago:productos")).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenThrow(timeout);

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class,
                () -> gestoPagoService.obtenerListaProductos());

        assertEquals(504, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Timeout al conectar"));
    }

    // -------------------------------------------------------------------------
    // Escenario 6: Respuesta con status ERROR (error lógico del negocio)
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_errorLogico() throws Exception {
        // Arrange
        String errorJson = "{\"status\":\"ERROR\",\"message\":\"Error interno en GestoPago\"}";
        ProductListResponse errorResponse = new ProductListResponse();
        errorResponse.setStatus("ERROR");
        errorResponse.setMessage("Error interno en GestoPago");

        when(valueOperations.get("gestopago:productos")).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenReturn(errorJson);
        when(objectMapper.readValue(errorJson, ProductListResponse.class)).thenReturn(errorResponse);

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class,
                () -> gestoPagoService.obtenerListaProductos());

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Error en la respuesta del servicio externo: Error interno en GestoPago"));
    }

    // -------------------------------------------------------------------------
    // Escenario 7: Error inesperado (500)
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_errorInesperado() {
        // Arrange
        when(valueOperations.get("gestopago:productos")).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class,
                () -> gestoPagoService.obtenerListaProductos());

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Error interno al procesar la integración"));
    }
}
