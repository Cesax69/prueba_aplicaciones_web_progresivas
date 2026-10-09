package com.proyecto.servicios.entity.clientes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "catalogos")
@Getter
@Setter
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false, length = 50, columnDefinition = "TEXT")
    private String tipo;

    @Column(name = "clave", nullable = false, length = 20, columnDefinition = "TEXT")
    private String clave;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;
}
