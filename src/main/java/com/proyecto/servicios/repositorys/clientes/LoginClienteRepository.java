package com.proyecto.servicios.repositorys.clientes;

import com.proyecto.servicios.entity.clientes.LoginCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginClienteRepository extends JpaRepository<LoginCliente, Long> {

    Optional<LoginCliente> findByUsuario(String usuario);

    Optional<LoginCliente> findByClienteId(Long clienteId);
}
