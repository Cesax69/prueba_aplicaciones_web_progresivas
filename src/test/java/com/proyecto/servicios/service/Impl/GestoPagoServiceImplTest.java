package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoClient;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.model.gestopago.ProductDTO;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestoPagoServiceImplTest {

    @Mock
    private GestoPagoClient gestoPagoClient;

    @InjectMocks
    private GestoPagoServiceImpl gestoPagoService;

    private Request request;

    @BeforeEach
    void setUp() {
        request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, new RequestTemplate());
    }

    @Test
    void obtenerListaProductos_Success() {
        // Arrange
        ProductListResponse mockResponse = new ProductListResponse();
        mockResponse.setStatus("OK");
        ProductDTO product = new ProductDTO("1", "Producto 1", "Desc", BigDecimal.TEN, "Cat", true);
        mockResponse.setData(Collections.singletonList(product));

        when(gestoPagoClient.getProductList()).thenReturn(mockResponse);

        // Act
        ProductListResponse result = gestoPagoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals(1, result.getData().size());
        assertEquals("Producto 1", result.getData().get(0).getName());
        
        verify(gestoPagoClient, times(1)).getProductList();
    }

    @Test
    void obtenerListaProductos_ErrorLogico() {
        // Arrange
        ProductListResponse mockResponse = new ProductListResponse();
        mockResponse.setStatus("ERROR");
        mockResponse.setMessage("Error interno en GestoPago");

        when(gestoPagoClient.getProductList()).thenReturn(mockResponse);

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class, () -> {
            gestoPagoService.obtenerListaProductos();
        });

        assertTrue(exception.getMessage().contains("Error en la respuesta del servicio externo: Error interno en GestoPago"));
        assertEquals(500, exception.getStatusCode());
        
        verify(gestoPagoClient, times(1)).getProductList();
    }

    @Test
    void obtenerListaProductos_Unauthorized() {
        // Arrange
        FeignException.Unauthorized unauthorized = new FeignException.Unauthorized(
                "Unauthorized", request, null, new HashMap<>());
        
        when(gestoPagoClient.getProductList()).thenThrow(unauthorized);

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class, () -> {
            gestoPagoService.obtenerListaProductos();
        });

        assertEquals(401, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Error de autenticación"));
        
        verify(gestoPagoClient, times(1)).getProductList();
    }

    @Test
    void obtenerListaProductos_Timeout() {
        // Arrange
        FeignException.GatewayTimeout timeout = new FeignException.GatewayTimeout(
                "Gateway Timeout", request, null, new HashMap<>());

        when(gestoPagoClient.getProductList()).thenThrow(timeout);

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class, () -> {
            gestoPagoService.obtenerListaProductos();
        });

        assertEquals(504, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Timeout al conectar"));
        
        verify(gestoPagoClient, times(1)).getProductList();
    }

    @Test
    void obtenerListaProductos_UnexpectedError() {
        // Arrange
        when(gestoPagoClient.getProductList()).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        GestoPagoException exception = assertThrows(GestoPagoException.class, () -> {
            gestoPagoService.obtenerListaProductos();
        });

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Error interno al procesar la integración"));
        
        verify(gestoPagoClient, times(1)).getProductList();
    }
}
