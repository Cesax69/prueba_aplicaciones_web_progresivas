package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.model.clientes.ClienteRequestDTO;
import com.proyecto.servicios.model.clientes.ClienteResponseDTO;
import com.proyecto.servicios.model.clientes.CuentaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ClienteService {

    ClienteResponseDTO registrarCliente(ClienteRequestDTO request);

    List<ClienteResponseDTO> obtenerTodosLosClientes();

    ClienteResponseDTO obtenerClientePorId(Long id);

    ClienteResponseDTO obtenerClientePorCurp(String curp);

    ClienteResponseDTO obtenerClientePorRfc(String rfc);

    ClienteResponseDTO obtenerClientePorCorreo(String correo);

    ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO request);

    void desactivarCliente(Long id);

    // Consultas específicas
    List<ClienteResponseDTO> obtenerClientesActivos();

    List<ClienteResponseDTO> obtenerClientesPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);

    CuentaResponseDTO obtenerCuentaPorNumero(String numeroCuenta);

    List<CuentaResponseDTO> obtenerCuentasActivas();
}
