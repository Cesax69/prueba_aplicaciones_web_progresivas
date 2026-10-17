package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.proyecto.servicios.model.GenericResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClienteResponseDTO extends GenericResponse {
    private Long id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private String curp;
    private String rfc;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    private String correo;
    private String telefonoMovil;
    private String telefonoAlternativo;
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    private Boolean activo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro;

    // Domicilio
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private String pais;

    // Cuenta (resumen)
    private String numeroCuenta;
    private String estatusCuenta;
}
