package com.proyecto.servicios.repositorys.gestopago;

import com.proyecto.servicios.entity.gestopago.GestoPagoProductCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GestoPagoProductCacheRepository extends JpaRepository<GestoPagoProductCache, Integer> {

    /**
     * Obtiene el registro de caché más reciente (solo debe haber uno activo).
     */
    Optional<GestoPagoProductCache> findTopByOrderByFechaActualizacionDesc();
}
