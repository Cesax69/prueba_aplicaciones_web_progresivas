package com.proyecto.servicios.service.clientes.impl;

import com.proyecto.servicios.entity.clientes.*;
import com.proyecto.servicios.enums.EstatusClienteEnum;
import com.proyecto.servicios.enums.EstatusCuentaEnum;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.clientes.ClienteRequestDTO;
import com.proyecto.servicios.model.clientes.ClienteResponseDTO;
import com.proyecto.servicios.model.clientes.CuentaResponseDTO;
import com.proyecto.servicios.repositorys.clientes.*;
import com.proyecto.servicios.service.clientes.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final LoginClienteRepository loginRepository;
    private final DatoBiometricoRepository biometricoRepository;
    private final CatalogoRepository catalogoRepository;

    @Override
    @Transactional
    public ClienteResponseDTO registrarCliente(ClienteRequestDTO request) {
        log.info("Iniciando registro de cliente con CURP: {}", request.getCurp());
        
        validarReglasNegocio(request);

        // 1. Crear Cliente
        Cliente cliente = new Cliente();
        mapearDatosPersonales(request, cliente);
        cliente = clienteRepository.save(cliente);

        // 2. Crear Domicilio
        Domicilio domicilio = new Domicilio();
        mapearDomicilio(request, domicilio);
        domicilio.setCliente(cliente);
        cliente.setDomicilio(domicilio);

        // 3. Crear Cuenta Bancaria
        Cuenta cuenta = crearCuentaBancaria(cliente);
        cliente.setCuenta(cuenta);

        // 4. Crear Login (Opcional si vienen datos)
        if (request.getUsuario() != null && !request.getUsuario().isBlank() && 
            request.getContrasena() != null && !request.getContrasena().isBlank()) {
            LoginCliente login = crearLogin(cliente, request.getUsuario(), request.getContrasena());
            cliente.setLoginCliente(login);
        }

        // 5. Crear Datos Biométricos (Ej: MediaPipe Embedding Hash)
        if (request.getHashBiometrico() != null && !request.getHashBiometrico().isBlank()) {
            crearDatoBiometrico(cliente, request.getHashBiometrico());
        }

        clienteRepository.save(cliente);
        log.info("Cliente registrado exitosamente con ID: {}", cliente.getId());
        
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodosLosClientes() {
        return clienteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con ID: " + id));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorCurp(String curp) {
        Cliente cliente = clienteRepository.findByCurp(curp)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con CURP: " + curp));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorRfc(String rfc) {
        Cliente cliente = clienteRepository.findByRfc(rfc)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con RFC: " + rfc));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorCorreo(String correo) {
        Cliente cliente = clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new ClienteNoEncontradoException("El correo electrónico '" + correo + "' no está registrado en el sistema"));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + numeroCuenta));
        return mapToResponse(cuenta.getCliente());
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con ID: " + id));

        // Actualizar datos permitidos (NO CURP, NO RFC)
        cliente.setNombre(request.getNombre());
        cliente.setSegundoNombre(request.getSegundoNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setSexo(request.getSexo());
        // Resolver descripción de nacionalidad desde el catálogo
        String descNacionalidad = catalogoRepository.findById(request.getNacionalidadId())
                .filter(c -> "NACIONALIDAD".equals(c.getTipo()) && Boolean.TRUE.equals(c.getActivo()))
                .map(cat -> cat.getDescripcion())
                .orElseThrow(() -> new ValidacionException(
                        "La nacionalidad con ID " + request.getNacionalidadId() + " no existe en el catálogo"));
        cliente.setNacionalidad(descNacionalidad);
        cliente.setEstadoCivil(request.getEstadoCivil());
        cliente.setOcupacion(request.getOcupacion());
        cliente.setEmpresa(request.getEmpresa());
        cliente.setIngresoMensual(request.getIngresoMensual());
        
        // Correo (validar unicidad si cambia)
        if (!cliente.getCorreo().equals(request.getCorreo())) {
            if (clienteRepository.existsByCorreo(request.getCorreo())) {
                throw new CorreoDuplicadoException("El correo ya está registrado en otra cuenta");
            }
            cliente.setCorreo(request.getCorreo());
        }
        
        cliente.setTelefonoMovil(request.getTelefonoMovil());
        cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());

        // Actualizar Domicilio
        if (cliente.getDomicilio() != null) {
            mapearDomicilio(request, cliente.getDomicilio());
        }

        cliente = clienteRepository.save(cliente);
        return mapToResponse(cliente);
    }

    @Override
    @Transactional
    public void desactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con ID: " + id));
        
        cliente.setActivo(false);
        
        // Si se desactiva el cliente, desactivamos la cuenta también
        if (cliente.getCuenta() != null) {
            cliente.getCuenta().setEstatus(EstatusCuentaEnum.INACTIVA.getCodigo());
        }
        if (cliente.getLoginCliente() != null) {
            cliente.getLoginCliente().setActivo(false);
        }
        
        clienteRepository.save(cliente);
        log.info("Cliente {} desactivado correctamente", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerClientesActivos() {
        return clienteRepository.findByActivoTrueOrderByFechaRegistroAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerClientesPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return clienteRepository.findByFechaRegistroBetweenOrderByFechaRegistroAsc(inicio, fin).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerCuentaPorNumero(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + numeroCuenta));
        return mapToCuentaResponse(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> obtenerCuentasActivas() {
        return cuentaRepository.findByEstatusOrderByFechaAperturaAsc(EstatusCuentaEnum.ACTIVA.getCodigo()).stream()
                .map(this::mapToCuentaResponse)
                .collect(Collectors.toList());
    }

    // --- Métodos Privados Auxiliares ---

    private void validarReglasNegocio(ClienteRequestDTO request) {
        // Validar mayoría de edad
        if (Period.between(request.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new ValidacionException("El cliente debe ser mayor de edad (18 años o más)");
        }
        // Validar que el ID de nacionalidad exista en el catálogo
        catalogoRepository.findById(request.getNacionalidadId())
                .filter(c -> "NACIONALIDAD".equals(c.getTipo()) && Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new ValidacionException(
                        "La nacionalidad con ID " + request.getNacionalidadId() +
                        " no existe en el catálogo. Consulta GET /api/v1/catalogos/NACIONALIDAD"));
        // Validar unicidad
        if (clienteRepository.existsByCurp(request.getCurp())) {
            throw new CurpDuplicadaException("La CURP ya se encuentra registrada");
        }
        if (clienteRepository.existsByRfc(request.getRfc())) {
            throw new RfcDuplicadoException("El RFC ya se encuentra registrado");
        }
        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new CorreoDuplicadoException("El correo electrónico ya se encuentra registrado");
        }
    }

    private void mapearDatosPersonales(ClienteRequestDTO req, Cliente c) {
        c.setNombre(req.getNombre());
        c.setSegundoNombre(req.getSegundoNombre());
        c.setApellidoPaterno(req.getApellidoPaterno());
        c.setApellidoMaterno(req.getApellidoMaterno());
        c.setFechaNacimiento(req.getFechaNacimiento());
        c.setCurp(req.getCurp());
        c.setRfc(req.getRfc());
        c.setSexo(req.getSexo());
        // Resolver descripción de nacionalidad desde el catálogo
        String descNacionalidad = catalogoRepository.findById(req.getNacionalidadId())
                .map(cat -> cat.getDescripcion())
                .orElse("Desconocida");
        c.setNacionalidad(descNacionalidad);
        c.setEstadoCivil(req.getEstadoCivil());
        c.setCorreo(req.getCorreo());
        c.setTelefonoMovil(req.getTelefonoMovil());
        c.setTelefonoAlternativo(req.getTelefonoAlternativo());
        c.setOcupacion(req.getOcupacion());
        c.setEmpresa(req.getEmpresa());
        c.setIngresoMensual(req.getIngresoMensual());
        c.setActivo(true);
    }

    private void mapearDomicilio(ClienteRequestDTO req, Domicilio d) {
        d.setCalle(req.getCalle());
        d.setNumeroExterior(req.getNumeroExterior());
        d.setNumeroInterior(req.getNumeroInterior());
        d.setColonia(req.getColonia());
        d.setMunicipio(req.getMunicipio());
        d.setEstado(req.getEstado());
        d.setCodigoPostal(req.getCodigoPostal());
        d.setPais(req.getPais());
        d.setActivo(true);
    }

    private Cuenta crearCuentaBancaria(Cliente cliente) {
        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta(generarNumeroCuentaUnico());
        cuenta.setEstatus(EstatusCuentaEnum.ACTIVA.getCodigo());
        
        Saldo saldo = new Saldo();
        saldo.setCuenta(cuenta);
        // Saldo inicial definido por el sistema
        saldo.setSaldo(new BigDecimal("100.00")); 
        saldo.setConcepto("SALDO INICIAL POR APERTURA");
        
        cuenta.setSaldo(saldo);
        return cuenta;
    }

    private String generarNumeroCuentaUnico() {
        Random random = new Random();
        String numero;
        do {
            // Generar 10 dígitos aleatorios
            long randNum = (long) (random.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
            numero = String.valueOf(randNum);
        } while (cuentaRepository.existsByNumeroCuenta(numero));
        return numero;
    }

    private LoginCliente crearLogin(Cliente cliente, String usuario, String contrasenaRaw) {
        LoginCliente login = new LoginCliente();
        login.setCliente(cliente);
        login.setUsuario(usuario);
        login.setContrasenaHash(cifrarSHA256(contrasenaRaw));
        login.setActivo(true);
        login.setBloqueado(false);
        login.setIntentosFallidos(0);
        return login;
    }

    private void crearDatoBiometrico(Cliente cliente, String hash) {
        DatoBiometrico db = new DatoBiometrico();
        db.setCliente(cliente);
        db.setTipo("FACIAL_MEDIAPIPE");
        db.setHashBiometrico(hash);
        db.setActivo(true);
        biometricoRepository.save(db);
    }

    private String cifrarSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error cifrando contraseña", e);
        }
    }

    private ClienteResponseDTO mapToResponse(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setSegundoNombre(cliente.getSegundoNombre());
        dto.setApellidoPaterno(cliente.getApellidoPaterno());
        dto.setApellidoMaterno(cliente.getApellidoMaterno());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setCurp(cliente.getCurp());
        dto.setRfc(cliente.getRfc());
        dto.setSexo(cliente.getSexo());
        dto.setNacionalidad(cliente.getNacionalidad());
        dto.setEstadoCivil(cliente.getEstadoCivil());
        dto.setCorreo(cliente.getCorreo());
        dto.setTelefonoMovil(cliente.getTelefonoMovil());
        dto.setTelefonoAlternativo(cliente.getTelefonoAlternativo());
        dto.setOcupacion(cliente.getOcupacion());
        dto.setEmpresa(cliente.getEmpresa());
        dto.setIngresoMensual(cliente.getIngresoMensual());
        dto.setActivo(cliente.getActivo());
        dto.setFechaRegistro(cliente.getFechaRegistro());

        if (cliente.getDomicilio() != null) {
            dto.setDomicilioId(cliente.getDomicilio().getId());
            dto.setCalle(cliente.getDomicilio().getCalle());
            dto.setNumeroExterior(cliente.getDomicilio().getNumeroExterior());
            dto.setNumeroInterior(cliente.getDomicilio().getNumeroInterior());
            dto.setColonia(cliente.getDomicilio().getColonia());
            dto.setMunicipio(cliente.getDomicilio().getMunicipio());
            dto.setEstado(cliente.getDomicilio().getEstado());
            dto.setCodigoPostal(cliente.getDomicilio().getCodigoPostal());
            dto.setPais(cliente.getDomicilio().getPais());
        }

        if (cliente.getCuenta() != null) {
            dto.setNumeroCuenta(cliente.getCuenta().getNumeroCuenta());
            dto.setEstatusCuenta(cliente.getCuenta().getEstatus());
        }

        return dto;
    }

    private CuentaResponseDTO mapToCuentaResponse(Cuenta cuenta) {
        CuentaResponseDTO dto = new CuentaResponseDTO();
        dto.setNumeroCuenta(cuenta.getNumeroCuenta());
        dto.setEstatus(cuenta.getEstatus());
        dto.setFechaApertura(cuenta.getFechaApertura());
        
        if (cuenta.getSaldo() != null) {
            dto.setSaldo(cuenta.getSaldo().getSaldo());
        } else {
            dto.setSaldo(BigDecimal.ZERO);
        }
        
        if (cuenta.getCliente() != null) {
            dto.setClienteId(cuenta.getCliente().getId());
            dto.setNombreCliente(cuenta.getCliente().getNombre() + " " + cuenta.getCliente().getApellidoPaterno());
        }
        
        return dto;
    }
}
