package com.proyecto.servicios.entity.clientes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "datos_biometricos")
@Getter
@Setter
public class DatoBiometrico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Tipo de dato biométrico (ej: HUELLA, IRIS, VOZ, CARA).
     */
    @Column(name = "tipo", nullable = false, columnDefinition = "TEXT")
    private String tipo;

    /**
     * Valor decimal del dato biométrico (BigDecimal en Java, NUMERIC en BD).
     * Se usa cuando el dato es continuo (ej: distancia de rasgos faciales).
     */
    @Column(name = "valor_decimal", precision = 18, scale = 8)
    private BigDecimal valorDecimal;

    /**
     * Valor entero del dato biométrico (Long en Java, BIGINT en BD).
     * Se usa cuando el dato es discreto o un identificador numérico.
     */
    @Column(name = "valor_entero")
    private Long valorEntero;

    /**
     * Hash cifrado del dato biométrico para comparación segura.
     */
    @Column(name = "hash_biometrico", nullable = false, columnDefinition = "TEXT")
    private String hashBiometrico;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
