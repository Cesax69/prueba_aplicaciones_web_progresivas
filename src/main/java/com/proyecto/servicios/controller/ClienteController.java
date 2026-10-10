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


    @PutMapping("/actualizar")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente enviando todos sus datos en JSON. La CURP viene incluida en el cuerpo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o formato incorrecto en el JSON",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(request.getCurp(), request));
    }

    @PostMapping("/desactivar")
    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente (baja lógica) recibiendo un JSON con su CURP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente desactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Formato de CURP inválido en JSON",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> desactivarCliente(
            @Valid @RequestBody com.proyecto.servicios.model.clientes.ClienteCurpRequestDTO peticion) {
        clienteService.desactivarCliente(peticion.getCurp());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/buscar")
    @Operation(summary = "Buscar cliente dinámicamente", description = "Permite buscar un cliente enviando un JSON con curp, rfc, correo o numeroCuenta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con el dato proporcionado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Formato de búsqueda inválido o más de un parámetro proporcionado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> buscarCliente(
            @Valid @RequestBody com.proyecto.servicios.model.clientes.ClienteBusquedaRequestDTO peticion) {

        String curp = peticion.getCurp();
        String rfc = peticion.getRfc();
        String correo = peticion.getCorreo();
        String numeroCuenta = peticion.getNumeroCuenta();

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

    @PostMapping("/rango-fechas")
    @Operation(summary = "Obtener clientes por rango de fechas de registro",
            description = "Filtra clientes enviando un JSON con fecha de inicio y fin. Formato: yyyy-MM-dd")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Formato de fecha inválido o parámetros faltantes",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ClienteResponseDTO>> obtenerPorRangoFechas(
            @Valid @RequestBody com.proyecto.servicios.model.clientes.ClienteRangoFechasRequestDTO peticion) {
        return ResponseEntity.ok(clienteService.obtenerClientesPorRangoFechas(peticion.getInicio(), peticion.getFin()));
    }
}
