package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un producto individual de GestoPago.
 * <p>
 * Los datos vienen como atributos XML del elemento {@code <producto>}:
 * <pre>
 * {@code
 * <producto
 *   servicio="ABIB"
 *   producto="ABIB 100"
 *   idServicio="2284"
 *   idProducto="14302"
 *   idCatTipoServicio="13"
 *   tipoFront="1"
 *   hasDigitoVerificador="false"
 *   precio="100.0"
 *   showAyuda="false"
 *   tipoReferencia="a"/>
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {

    @JacksonXmlProperty(isAttribute = true, localName = "idProducto")
    private String id;

    /** Nombre del producto */
    @JacksonXmlProperty(isAttribute = true, localName = "producto")
    private String name;

    /** Nombre del servicio al que pertenece el producto */
    @JacksonXmlProperty(isAttribute = true, localName = "servicio")
    private String service;

    /** ID del servicio */
    @JacksonXmlProperty(isAttribute = true, localName = "idServicio")
    private String serviceId;

    /** Precio del producto */
    @JacksonXmlProperty(isAttribute = true, localName = "precio")
    private String price;

    /** Tipo de referencia para el pago */
    @JacksonXmlProperty(isAttribute = true, localName = "tipoReferencia")
    private String referenceType;

    /** Tipo de front para clasificación de la interfaz */
    @JacksonXmlProperty(isAttribute = true, localName = "tipoFront")
    private String frontType;

    /** Indica si tiene dígito verificador */
    @JacksonXmlProperty(isAttribute = true, localName = "hasDigitoVerificador")
    private String hasVerificationDigit;

    /** ID de la categoría del tipo de servicio */
    @JacksonXmlProperty(isAttribute = true, localName = "idCatTipoServicio")
    private String categoryId;
}
