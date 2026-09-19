package com.proyecto.servicios.entity.gestopago;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad de respaldo (fallback) para almacenar en BD la lista de productos
 * cuando Redis no está disponible.
 */
@Entity
@Table(name = "gestopago_product_cache")
@Getter
@Setter
public class GestoPagoProductCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * JSON serializado de la lista de productos obtenida de GestoPago.
     */
    @Column(name = "productos_json", nullable = false, columnDefinition = "TEXT")
    private String productosJson;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
