package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.clientes.CatalogoConsultaRequestDTO;
import com.proyecto.servicios.model.clientes.CatalogoResponseDTO;
import com.proyecto.servicios.repositorys.clientes.CatalogoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/catalogos")
@Tag(name = "Catálogos", description = "Operaciones de consulta de catálogos del sistema")
public class CatalogoController {

    private final CatalogoRepository catalogoRepository;

    public CatalogoController(CatalogoRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }

    @PostMapping("/consultar")
    @Operation(summary = "Obtener catálogo por tipo", description = "Obtiene los elementos activos de un catálogo dado su tipo mediante JSON con @RequestBody (ej. NACIONALIDAD, SEXO, ESTADO_CIVIL, OCUPACION)")
    public ResponseEntity<List<CatalogoResponseDTO>> obtenerCatalogoPorTipo(@Valid @RequestBody CatalogoConsultaRequestDTO request) {
        List<CatalogoResponseDTO> catalogos = catalogoRepository.findByTipoAndActivoTrueOrderByOrdenAsc(request.getTipo().toUpperCase())
                .stream()
                .map(c -> {
                    CatalogoResponseDTO dto = new CatalogoResponseDTO();
                    dto.setId(c.getId());
                    dto.setTipo(c.getTipo());
                    dto.setClave(c.getClave());
                    dto.setDescripcion(c.getDescripcion());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(catalogos);
    }
}
