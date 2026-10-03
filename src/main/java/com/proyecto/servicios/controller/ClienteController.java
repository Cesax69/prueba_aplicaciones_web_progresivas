package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.GlobalExceptionHandler.ErrorResponse;
import com.proyecto.servicios.model.clientes.ClienteRequestDTO;
import com.proyecto.servicios.model.clientes.ClienteResponseDTO;
import com.proyecto.servicios.service.clientes.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Operaciones de gestión de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Registrar cliente", description = "Crea un nuevo cliente en el sistema con cuenta bancaria inicial de $100.00")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o formato incorrecto en el JSON",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "CURP, RFC o correo ya registrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> registrarCliente(@Valid @RequestBody ClienteRequestDTO request) {
        return new ResponseEntity<>(clienteService.registrarCliente(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los clientes", description = "Devuelve la lista completa de clientes registrados")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado con ese ID",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o formato incorrecto en el JSON",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente (baja lógica, no eliminación física)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente desactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> desactivarCliente(@PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar/curp")
    @Operation(summary = "Buscar cliente por CURP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con esa CURP",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> buscarPorCurp(@RequestParam String curp) {
        return ResponseEntity.ok(clienteService.obtenerClientePorCurp(curp));
    }

    @GetMapping("/buscar/rfc")
    @Operation(summary = "Buscar cliente por RFC")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con ese RFC",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> buscarPorRfc(@RequestParam String rfc) {
        return ResponseEntity.ok(clienteService.obtenerClientePorRfc(rfc));
    }

    @GetMapping("/buscar/correo")
    @Operation(summary = "Buscar cliente por correo electrónico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con ese correo",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> buscarPorCorreo(@RequestParam String correo) {
        return ResponseEntity.ok(clienteService.obtenerClientePorCorreo(correo));
    }

    @GetMapping("/buscar/cuenta")
    @Operation(summary = "Buscar cliente por número de cuenta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese número",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> buscarPorCuenta(@RequestParam String numeroCuenta) {
        return ResponseEntity.ok(clienteService.obtenerClientePorNumeroCuenta(numeroCuenta));
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener clientes activos", description = "Devuelve únicamente los clientes con estado activo")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(clienteService.obtenerClientesActivos());
    }

    @GetMapping("/rango-fechas")
    @Operation(summary = "Obtener clientes por rango de fechas de registro")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(clienteService.obtenerClientesPorRangoFechas(inicio, fin));
    }
}
