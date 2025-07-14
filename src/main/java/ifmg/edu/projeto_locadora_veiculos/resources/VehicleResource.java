package ifmg.edu.projeto_locadora_veiculos.resources;

import ifmg.edu.projeto_locadora_veiculos.dto.VehicleDTO;
import ifmg.edu.projeto_locadora_veiculos.services.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/vehicle")
@Tag(name = "Veículos", description = "API para gerenciamento de veículos")
public class VehicleResource {

    @Autowired
    private VehicleService vehicleService;

    @Operation(summary = "Listar veículos", description = "Lista todos os veículos com paginação")
    @GetMapping
    public ResponseEntity<Page<VehicleDTO>> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "id") String orderBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction), orderBy);
        Page<VehicleDTO> list = vehicleService.findAll(pageable);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Buscar veículo por ID", description = "Retorna os dados de um veículo específico")
    @GetMapping(value = "/{id}")
    public ResponseEntity<VehicleDTO> findById(@PathVariable Long id) {
        VehicleDTO dto = vehicleService.findById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Inserir veículo", description = "Adiciona um novo veículo")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleDTO> insert(@RequestBody VehicleDTO dto) {
        VehicleDTO newDto = vehicleService.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newDto.getId()).toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @Operation(summary = "Atualizar veículo", description = "Atualiza um veículo existente")
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleDTO> update(@PathVariable Long id, @RequestBody VehicleDTO dto) {
        VehicleDTO updatedDto = vehicleService.update(id, dto);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Deletar veículo", description = "Remove um veículo do sistema")
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}