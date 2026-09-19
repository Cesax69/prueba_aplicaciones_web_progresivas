package com.proyecto.servicios.enums;

public enum DataSourceEnum {
    EXITO(0, "Éxito"),
    ERROR_REDIS(1, "Error en Redis, usando BD"),
    ERROR_BD(2, "Error en BD");

    private final int codigo;
    private final String descripcion;

    DataSourceEnum(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
