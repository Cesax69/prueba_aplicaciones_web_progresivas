package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mapeo de la respuesta XML del servicio externo GestoPago.
 * <p>
 * Estructura real de la API:
 * <pre>
 * {@code
 * <RESPONSE>
 *   <MENSAJE>...</MENSAJE>
 *   <PRODUCTOS>
 *     <producto servicio="..." producto="..." idProducto="..." precio="..." tipoFront="..." .../>
 *   </PRODUCTOS>
 * </RESPONSE>
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "RESPONSE")
public class ProductListResponse {

    /** Mensaje de respuesta del servidor (elemento XML: MENSAJE) */
    @JacksonXmlProperty(localName = "MENSAJE")
    private String message;

    /** Lista de productos (elemento contenedor: PRODUCTOS, elementos hijos: producto) */
    @JacksonXmlElementWrapper(localName = "PRODUCTOS")
    @JacksonXmlProperty(localName = "producto")
    private List<ProductDTO> data;

    /**
     * Campo de estado calculado: "OK" si la respuesta tiene productos,
     * "VACIO" si la lista está vacía, "ERROR" si hubo fallo.
     */
    private String status;

    /**
     * Calcula el estado basado en los datos recibidos del XML.
     * Se invoca manualmente después del parseo.
     */
    public void calcularStatus() {
        if (data != null && !data.isEmpty()) {
            this.status = "OK";
        } else if (message != null && !message.isEmpty()) {
            this.status = "VACIO";
        } else {
            this.status = "OK";
        }
    }
}
