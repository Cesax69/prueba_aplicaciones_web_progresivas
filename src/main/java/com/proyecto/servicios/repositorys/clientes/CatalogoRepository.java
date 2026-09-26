package com.proyecto.servicios.repositorys.clientes;

import com.proyecto.servicios.entity.clientes.Catalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {

    /** Obtiene catálogos por tipo, ordenados de menor a mayor según el campo orden */
    List<Catalogo> findByTipoAndActivoTrueOrderByOrdenAsc(String tipo);
}
