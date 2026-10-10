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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Validated
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

    @GetMapping("/{curp}")
    @Operation(summary = "Obtener cliente por CURP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado con esa CURP",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Formato de CURP inválido",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> obtenerPorCurp(
            @PathVariable 
            @NotBlank(message = "La CURP no puede estar vacía")
            @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "El formato de la CURP es inválido. Solo se aceptan 18 caracteres alfanuméricos en mayúscula") 
            String curp) {
        return ResponseEntity.ok(clienteService.obtenerClientePorCurp(curp));
    }

    @PutMapping("/{curp}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente buscando por su CURP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o formato incorrecto en el JSON",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable 
            @NotBlank(message = "La CURP no puede estar vacía")
            @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "El formato de la CURP es inválido. Solo se aceptan 18 caracteres alfanuméricos en mayúscula") 
            String curp,
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(curp, request));
    }

    @DeleteMapping("/{curp}")
    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente (baja lógica, no eliminación física) buscándolo por CURP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente desactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Formato de CURP inválido",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> desactivarCliente(
            @PathVariable 
            @NotBlank(message = "La CURP no puede estar vacía")
            @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "El formato de la CURP es inválido. Solo se aceptan 18 caracteres alfanuméricos en mayúscula") 
            String curp) {
        clienteService.desactivarCliente(curp);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar cliente dinámicamente", description = "Permite buscar un cliente proporcionando exactamente un parámetro: curp, rfc, correo o numeroCuenta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con el dato proporcionado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Formato de búsqueda inválido o más de un parámetro proporcionado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> buscarCliente(
            @RequestParam(required = false) 
            @Pattern(regexp = "^$|^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "El formato de la CURP es inválido. Solo se aceptan 18 caracteres alfanuméricos en mayúscula") 
            String curp,
            
            @RequestParam(required = false) 
            @Pattern(regexp = "^$|^[A-Z&Ñ]{3,4}\\d{6}[A-Z\\d]{3}$", message = "El formato del RFC es inválido. Debe tener 12 o 13 caracteres alfanuméricos en mayúscula") 
            String rfc,
            
            @RequestParam(required = false) 
            @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "El formato del correo es inválido") 
            String correo,
            
            @RequestParam(required = false) 
            @Pattern(regexp = "^$|^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos numéricos") 
            String numeroCuenta) {

        if ((curp == null || curp.isBlank()) && (rfc == null || rfc.isBlank()) && 
            (correo == null || correo.isBlank()) && (numeroCuenta == null || numeroCuenta.isBlank())) {
            throw new com.proyecto.servicios.exception.ValidacionException("Debe proporcionar al menos un criterio de búsqueda (curp, rfc, correo o numeroCuenta)");
        }

        ClienteResponseDTO cliente = null;

        // Buscar por el primer criterio disponible
        if (curp != null && !curp.isBlank()) {
            cliente = clienteService.obtenerClientePorCurp(curp);
        } else if (rfc != null && !rfc.isBlank()) {
            cliente = clienteService.obtenerClientePorRfc(rfc);
        } else if (correo != null && !correo.isBlank()) {
            cliente = clienteService.obtenerClientePorCorreo(correo);
        } else if (numeroCuenta != null && !numeroCuenta.isBlank()) {
            cliente = clienteService.obtenerClientePorNumeroCuenta(numeroCuenta);
        }

        // Si se encontraron datos, validar que coincidan con los demás parámetros proporcionados
        if (cliente != null) {
            if (curp != null && !curp.isBlank() && !curp.equals(cliente.getCurp())) {
                throw new com.proyecto.servicios.exception.ClienteNoEncontradoException("Los datos proporcionados no coinciden con un mismo cliente (CURP no coincide)");
            }
            if (rfc != null && !rfc.isBlank() && !rfc.equals(cliente.getRfc())) {
                throw new com.proyecto.servicios.exception.ClienteNoEncontradoException("Los datos proporcionados no coinciden con un mismo cliente (RFC no coincide)");
            }
            if (correo != null && !correo.isBlank() && !correo.equals(cliente.getCorreo())) {
                throw new com.proyecto.servicios.exception.ClienteNoEncontradoException("Los datos proporcionados no coinciden con un mismo cliente (Correo no coincide)");
            }
            if (numeroCuenta != null && !numeroCuenta.isBlank() && !numeroCuenta.equals(cliente.getNumeroCuenta())) {
                throw new com.proyecto.servicios.exception.ClienteNoEncontradoException("Los datos proporcionados no coinciden con un mismo cliente (Número de cuenta no coincide)");
            }
        }

        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener clientes activos", description = "Devuelve únicamente los clientes con estado activo")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(clienteService.obtenerClientesActivos());
    }

    @GetMapping("/rango-fechas")
    @Operation(summary = "Obtener clientes por rango de fechas de registro",
            description = "Filtra clientes por fecha de registro. Formato: yyyy-MM-dd (ejemplo: 2024-01-01)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Formato de fecha inválido (use yyyy-MM-dd) o parámetro faltante",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ClienteResponseDTO>> obtenerPorRangoFechas(
            @RequestParam @NotNull(message = "La fecha de inicio es requerida. Formato: yyyy-MM-dd")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @NotNull(message = "La fecha de fin es requerida. Formato: yyyy-MM-dd")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(clienteService.obtenerClientesPorRangoFechas(inicio, fin));
    }
}
