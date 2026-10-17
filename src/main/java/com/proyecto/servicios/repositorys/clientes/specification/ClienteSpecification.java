package com.proyecto.servicios.repositorys.clientes.specification;

import com.proyecto.servicios.entity.clientes.Cliente;
import com.proyecto.servicios.model.clientes.ClienteBusquedaRequestDTO;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ClienteSpecification {

    public static Specification<Cliente> conFiltros(ClienteBusquedaRequestDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filtro.getId()));
            }

            if (StringUtils.hasText(filtro.getCurp())) {
                predicates.add(cb.equal(cb.upper(root.get("curp")), filtro.getCurp().trim().toUpperCase()));
            }

            if (StringUtils.hasText(filtro.getRfc())) {
                predicates.add(cb.equal(cb.upper(root.get("rfc")), filtro.getRfc().trim().toUpperCase()));
            }

            if (StringUtils.hasText(filtro.getCorreo())) {
                predicates.add(cb.equal(cb.lower(root.get("correo")), filtro.getCorreo().trim().toLowerCase()));
            }

            if (StringUtils.hasText(filtro.getNumeroCuenta())) {
                predicates.add(cb.equal(root.join("cuenta", JoinType.LEFT).get("numeroCuenta"), filtro.getNumeroCuenta().trim()));
            }

            if (StringUtils.hasText(filtro.getNombre())) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.getNombre().trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filtro.getApellidoPaterno())) {
                predicates.add(cb.like(cb.lower(root.get("apellidoPaterno")), "%" + filtro.getApellidoPaterno().trim().toLowerCase() + "%"));
            }

            if (filtro.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), filtro.getActivo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
