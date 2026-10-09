package com.proyecto.servicios.enums;

public enum EstatusCuentaEnum {
    ACTIVA("ACTIVA", "Cuenta activa"),
    INACTIVA("INACTIVA", "Cuenta inactiva"),
    BLOQUEADA("BLOQUEADA", "Cuenta bloqueada");

    private final String codigo;
    private final String descripcion;

    EstatusCuentaEnum(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
}
