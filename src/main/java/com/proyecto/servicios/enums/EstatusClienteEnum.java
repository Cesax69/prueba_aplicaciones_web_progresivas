package com.proyecto.servicios.enums;

public enum EstatusClienteEnum {
    ACTIVO(1, "Activo"),
    INACTIVO(0, "Inactivo");

    private final int codigo;
    private final String descripcion;

    EstatusClienteEnum(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
}
