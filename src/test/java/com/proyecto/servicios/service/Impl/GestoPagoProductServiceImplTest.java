package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoProductCache;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.enums.DataSourceEnum;
import com.proyecto.servicios.model.gestopago.ProductDTO;
import com.proyecto.servicios.model.gestopago.ProductGroupDTO;
import com.proyecto.servicios.model.gestopago.ProductGroupedResponse;
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
    private ProductListResponse mockFlatResponse;
    private ProductGroupedResponse mockGroupedResponse;
    private GestoPagoProductCache mockDbCache;

    private static final String FLAT_JSON_RESPONSE = "{\"status\":\"OK\",\"message\":null,\"data\":[{\"id\":\"1\",\"name\":\"Producto 1\",\"frontType\":\"1\"}]}";
    private static final String GROUPED_JSON_RESPONSE = "{\"status\":\"OK\",\"message\":null,\"totalProductos\":1,\"totalGrupos\":1,\"grupos\":[{\"tipoFront\":\"1\",\"total\":1,\"productos\":[{\"id\":\"1\",\"name\":\"Producto 1\",\"frontType\":\"1\"}]}]}";
    private static final String REDIS_KEY = "gestopago:productos:agrupados";

    @BeforeEach
    void setUp() {
        request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, new RequestTemplate());

        ProductDTO product = new ProductDTO("1", "Producto 1", "Servicio 1", "100", "10.0", "a", "1", "false", "11");

        mockFlatResponse = new ProductListResponse();
        mockFlatResponse.setStatus("OK");
        mockFlatResponse.setData(List.of(product));

        ProductGroupDTO group = new ProductGroupDTO("1", 1, List.of(product));
        mockGroupedResponse = new ProductGroupedResponse();
        mockGroupedResponse.setStatus("OK");
        mockGroupedResponse.setGrupos(List.of(group));
        
        mockDbCache = new GestoPagoProductCache();
        mockDbCache.setProductosJson(GROUPED_JSON_RESPONSE);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // -------------------------------------------------------------------------
    // Escenario 1: Sirve desde Redis cuando hay datos en caché
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_desdeRedis_exitoso() throws Exception {
        // Arrange
        when(valueOperations.get(REDIS_KEY)).thenReturn(GROUPED_JSON_RESPONSE);
        when(objectMapper.readValue(GROUPED_JSON_RESPONSE, ProductGroupedResponse.class)).thenReturn(mockGroupedResponse);

        // Act
        ProductListResponse result = gestoPagoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals(DataSourceEnum.EXITO.getCodigo(), result.getCodigoCache());
        // No debe llamar al cliente externo
        verifyNoInteractions(gestoPagoProductClient);
    }

    // -------------------------------------------------------------------------
    // Escenario 2: Redis vacío → llama al servicio externo y guarda en Redis
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_redisVacio_consultaExternoYGuardaEnRedis() throws Exception {
        // Arrange
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenReturn(FLAT_JSON_RESPONSE);
        when(objectMapper.readValue(FLAT_JSON_RESPONSE, ProductListResponse.class)).thenReturn(mockFlatResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn(GROUPED_JSON_RESPONSE);

        // Act
        ProductGroupedResponse result = gestoPagoService.obtenerProductosAgrupados();

        // Assert
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals(DataSourceEnum.EXITO.getCodigo(), result.getCodigoCache());
        // Debe guardar en Redis
        verify(valueOperations, times(1)).set(eq(REDIS_KEY), anyString(), anyLong(), eq(TimeUnit.HOURS));
        // No debe guardar en BD (Redis funcionó)
        verifyNoInteractions(productCacheRepository);
    }

    // -------------------------------------------------------------------------
    // Escenario 3: Redis falla, externo falla → lee de BD como fallback
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_redisVacio_externoFalla_leeDeBd() throws Exception {
        // Arrange
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenThrow(new RuntimeException("API down"));
        
        when(productCacheRepository.findTopByOrderByFechaActualizacionDesc()).thenReturn(Optional.of(mockDbCache));
        when(objectMapper.readValue(GROUPED_JSON_RESPONSE, ProductGroupedResponse.class)).thenReturn(mockGroupedResponse);

        // Act
        ProductGroupedResponse result = gestoPagoService.obtenerProductosAgrupados();

        // Assert
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals(DataSourceEnum.ERROR_REDIS.getCodigo(), result.getCodigoCache());
    }

    // -------------------------------------------------------------------------
    // Escenario 4: Redis falla → external api success, saves to DB instead of Redis
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_redisFallaEscritura_guardaEnBd() throws Exception {
        // Arrange
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenReturn(FLAT_JSON_RESPONSE);
        when(objectMapper.readValue(FLAT_JSON_RESPONSE, ProductListResponse.class)).thenReturn(mockFlatResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn(GROUPED_JSON_RESPONSE);
        
        // Simular que Redis lanza excepción al escribir (guardarEnCache)
        doThrow(new RuntimeException("Redis connection refused"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        
        when(productCacheRepository.findTopByOrderByFechaActualizacionDesc())
                .thenReturn(Optional.of(new GestoPagoProductCache()));

        // Act
        ProductGroupedResponse result = gestoPagoService.obtenerProductosAgrupados();

        // Assert
        assertNotNull(result);
        assertEquals(DataSourceEnum.EXITO.getCodigo(), result.getCodigoCache());
        // Debe haber guardado en BD porque falló guardar en Redis
        verify(productCacheRepository, times(1)).save(any(GestoPagoProductCache.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 5: Falla TODO (Redis vacío, Externo error, BD vacía)
    // -------------------------------------------------------------------------
    @Test
    void obtenerListaProductos_fallaTodo() {
        // Arrange
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(tokenService.obtenerTokenActivo(any(), any())).thenReturn(Optional.of(new GestoPagoToken()));
        when(gestoPagoProductClient.getProductList(anyString(), any())).thenThrow(new RuntimeException("API down"));
        when(productCacheRepository.findTopByOrderByFechaActualizacionDesc()).thenReturn(Optional.empty()); // BD vacía

        // Act
        ProductGroupedResponse result = gestoPagoService.obtenerProductosAgrupados();

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertEquals(DataSourceEnum.ERROR_BD.getCodigo(), result.getCodigoCache());
    }
}
