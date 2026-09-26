package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.clientes.CuentaResponseDTO;
import com.proyecto.servicios.service.clientes.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final ClienteService clienteService;

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(clienteService.obtenerCuentaPorNumero(numeroCuenta));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CuentaResponseDTO>> obtenerCuentasActivas() {
        return ResponseEntity.ok(clienteService.obtenerCuentasActivas());
    }

    @GetMapping("/{numeroCuenta}/saldo")
    public ResponseEntity<String> consultarSaldo(@PathVariable String numeroCuenta) {
        CuentaResponseDTO cuenta = clienteService.obtenerCuentaPorNumero(numeroCuenta);
        return ResponseEntity.ok("El saldo disponible de la cuenta " + numeroCuenta + " es: $" + cuenta.getSaldo());
    }
}
