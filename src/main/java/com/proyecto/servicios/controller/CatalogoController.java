package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.clientes.CatalogoResponseDTO;
import com.proyecto.servicios.repositorys.clientes.CatalogoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/catalogos")
@RequiredArgsConstructor
@Tag(name = "Catálogos", description = "Operaciones de consulta de catálogos del sistema")
public class CatalogoController {

    private final CatalogoRepository catalogoRepository;

    @GetMapping("/{tipo}")
    @Operation(summary = "Obtener catálogo por tipo", description = "Obtiene los elementos activos de un catálogo dado su tipo (ej. NACIONALIDAD, SEXO, ESTADO_CIVIL, OCUPACION)")
    public ResponseEntity<List<CatalogoResponseDTO>> obtenerCatalogoPorTipo(@PathVariable String tipo) {
        List<CatalogoResponseDTO> catalogos = catalogoRepository.findByTipoAndActivoTrueOrderByOrdenAsc(tipo.toUpperCase())
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
