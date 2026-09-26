package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.clientes.ClienteRequestDTO;
import com.proyecto.servicios.model.clientes.ClienteResponseDTO;
import com.proyecto.servicios.service.clientes.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> registrarCliente(@Valid @RequestBody ClienteRequestDTO request) {
        return new ResponseEntity<>(clienteService.registrarCliente(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable Long id, 
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // --- Consultas adicionales solicitadas ---

    @GetMapping("/buscar/curp")
    public ResponseEntity<ClienteResponseDTO> buscarPorCurp(@RequestParam String curp) {
        return ResponseEntity.ok(clienteService.obtenerClientePorCurp(curp));
    }

    @GetMapping("/buscar/rfc")
    public ResponseEntity<ClienteResponseDTO> buscarPorRfc(@RequestParam String rfc) {
        return ResponseEntity.ok(clienteService.obtenerClientePorRfc(rfc));
    }

    @GetMapping("/buscar/correo")
    public ResponseEntity<ClienteResponseDTO> buscarPorCorreo(@RequestParam String correo) {
        return ResponseEntity.ok(clienteService.obtenerClientePorCorreo(correo));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(clienteService.obtenerClientesActivos());
    }

    @GetMapping("/rango-fechas")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(clienteService.obtenerClientesPorRangoFechas(inicio, fin));
    }
}
