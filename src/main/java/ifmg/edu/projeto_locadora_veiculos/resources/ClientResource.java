package ifmg.edu.projeto_locadora_veiculos.resources;

import ifmg.edu.projeto_locadora_veiculos.dto.ClientDTO;
import ifmg.edu.projeto_locadora_veiculos.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping(value = "/client")
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
public class ClientResource {

    @Autowired
    private ClientService clientService;

    @Operation(
            summary = "Listar todos os clientes com paginação",
            description = "Retorna uma página de clientes. Requer permissão de ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso",
                            content = @Content(schema = @Schema(implementation = ClientDTO.class)))
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClientDTO>> findAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "id") String orderBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction), orderBy);
        Page<ClientDTO> clients = clientService.findAll(pageable);

        clients.forEach(this::addHateoasLinks);

        return ResponseEntity.ok().body(clients);
    }

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna os dados de um cliente específico. Requer permissão de ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                            content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
            }
    )
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> findById(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id) {
        ClientDTO client = clientService.findById(id);
        addHateoasLinks(client);
        return ResponseEntity.ok().body(client);
    }

    @Operation(
            summary = "Inserir novo cliente",
            description = "Cria um novo cliente. Requer permissão de ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                            content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> insert(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do novo cliente",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            )
            @RequestBody ClientDTO clientDTO) {
        clientDTO.setPassword(clientDTO.getPhone()); // regra de negócio
        ClientDTO newClient = clientService.insert(clientDTO);
        addHateoasLinks(newClient);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newClient.getId()).toUri();
        return ResponseEntity.created(uri).body(newClient);
    }

    @Operation(
            summary = "Atualizar cliente existente",
            description = "Atualiza os dados de um cliente. Requer permissão de ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                            content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
            }
    )
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> update(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do cliente",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            )
            @RequestBody ClientDTO clientDTO) {
        ClientDTO updatedClient = clientService.update(id, clientDTO);
        addHateoasLinks(updatedClient);
        return ResponseEntity.ok().body(updatedClient);
    }

    @Operation(
            summary = "Deletar cliente",
            description = "Remove um cliente existente. Requer permissão de ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
            }
    )
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.ok("Cliente deletado com sucesso.");
    }

    private void addHateoasLinks(ClientDTO client) {
        client.add(linkTo(methodOn(ClientResource.class).findById(client.getId())).withSelfRel());
        client.add(linkTo(methodOn(ClientResource.class).findAll(0, 10, "ASC", "id")).withRel("list"));
        client.add(linkTo(methodOn(ClientResource.class).update(client.getId(), null)).withRel("update"));
        client.add(linkTo(methodOn(ClientResource.class).delete(client.getId())).withRel("delete"));
        client.add(linkTo(methodOn(ClientResource.class).insert(null)).withRel("insert"));
    }
}
