package ifmg.edu.projeto_locadora_veiculos.resources;

import ifmg.edu.projeto_locadora_veiculos.dto.ApiResponseDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.ReservationDTO;
import ifmg.edu.projeto_locadora_veiculos.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/reservation")
@Tag(name = "Reservas", description = "API para gerenciamento de reservas")
public class ReservationResource {

    @Autowired
    private ReservationService reservationService;

    @Operation(
            summary = "Listar reservas com paginação",
            description = "Retorna todas as reservas cadastradas no sistema. Requer permissão ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de reservas retornada com sucesso",
                            content = @Content(schema = @Schema(implementation = ReservationDTO.class)))
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservationDTO>> findAll(
            @Parameter(description = "Número da página", example = "0") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página", example = "10") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC") @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @Parameter(description = "Campo para ordenação", example = "id") @RequestParam(value = "orderBy", defaultValue = "id") String orderBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction), orderBy);
        Page<ReservationDTO> reservations = reservationService.findAll(pageable);
        reservations.forEach(this::addHateoasLinks);
        return ResponseEntity.ok().body(reservations);
    }

    @Operation(
            summary = "Buscar reserva por ID",
            description = "Retorna os dados de uma reserva específica. Requer permissão ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                            content = @Content(schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content)
            }
    )
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> findById(
            @Parameter(description = "ID da reserva", example = "1") @PathVariable Long id) {
        ReservationDTO dto = reservationService.findById(id);
        addHateoasLinks(dto);
        return ResponseEntity.ok().body(dto);
    }

    @Operation(
            summary = "Criar nova reserva",
            description = "Cria uma nova reserva. Requer permissão ADMIN/CLIENT.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reserva criada com sucesso",
                            content = @Content(schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponseDTO<ReservationDTO>> insert(
            @RequestBody(
                    description = "Dados da nova reserva",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ReservationDTO.class))
            )
            @org.springframework.web.bind.annotation.RequestBody ReservationDTO dto) {
        ReservationDTO newReservation = reservationService.insert(dto);
        addHateoasLinks(newReservation);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newReservation.getId()).toUri();

        ApiResponseDTO<ReservationDTO> response = new ApiResponseDTO<>(
                "Reserva criada com sucesso!",
                newReservation
        );

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
            summary = "Atualizar reserva",
            description = "Atualiza os dados de uma reserva existente. Requer permissão ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva atualizada com sucesso",
                            content = @Content(schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content)
            }
    )
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> update(
            @Parameter(description = "ID da reserva", example = "1") @PathVariable Long id,
            @RequestBody(
                    description = "Novos dados da reserva",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ReservationDTO.class))
            )
            @org.springframework.web.bind.annotation.RequestBody ReservationDTO dto) {
        ReservationDTO updated = reservationService.update(id, dto);
        addHateoasLinks(updated);
        return ResponseEntity.ok().body(updated);
    }

    @Operation(
            summary = "Deletar reserva",
            description = "Remove uma reserva existente. Requer permissão ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Reserva deletada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content)
            }
    )
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @Parameter(description = "ID da reserva", example = "1") @PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.ok("Reserva deletada com sucesso.");
    }

    private void addHateoasLinks(ReservationDTO reservation) {
        reservation.add(linkTo(methodOn(ReservationResource.class).findById(reservation.getId())).withSelfRel());
        reservation.add(linkTo(methodOn(ReservationResource.class).findAll(0, 10, "ASC", "id")).withRel("list"));
        reservation.add(linkTo(methodOn(ReservationResource.class).update(reservation.getId(), null)).withRel("update"));
        reservation.add(linkTo(methodOn(ReservationResource.class).delete(reservation.getId())).withRel("delete"));
        reservation.add(linkTo(methodOn(ReservationResource.class).insert(null)).withRel("insert"));
    }
}
