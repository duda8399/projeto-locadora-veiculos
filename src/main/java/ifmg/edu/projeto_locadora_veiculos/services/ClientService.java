package ifmg.edu.projeto_locadora_veiculos.services;

import ifmg.edu.projeto_locadora_veiculos.dto.ClientDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Client;
import ifmg.edu.projeto_locadora_veiculos.entities.Role;
import ifmg.edu.projeto_locadora_veiculos.repositories.ClientRepository;
import ifmg.edu.projeto_locadora_veiculos.services.exceptions.DatabaseException;
import ifmg.edu.projeto_locadora_veiculos.services.exceptions.ResourceNotFound;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(produces = "application/json")
    @Operation(
            description = "Obtenha todos os clientes",
            summary = "Listar todos os clientes cadastrados",
            responses = {
                    @ApiResponse(description = "ok", responseCode = "200"),
            }
    )
    public Page<ClientDTO> findAll(Pageable pageable) {
        Page<Client> list = clientRepository.findAll(pageable);
        return list.map(ClientDTO::new);
    }

    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Cliente não encontrado"));
        return new ClientDTO(client);
    }

    @Transactional
    public ClientDTO insert(ClientDTO dto) {
        Client entity = new Client();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setRole(Role.CLIENT);
        entity = clientRepository.save(entity);
        return new ClientDTO(entity);
    }

    @Transactional
    public ClientDTO update(Long id, ClientDTO dto) {
        try {
            Client entity = clientRepository.getReferenceById(id);
            entity.setName(dto.getName());
            entity.setEmail(dto.getEmail());

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                entity.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            entity.setAddress(dto.getAddress());
            entity.setCity(dto.getCity());
            entity.setPhone(dto.getPhone());
            entity = clientRepository.save(entity);
            return new ClientDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFound("Cliente não encontrado: " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFound("Cliente não encontrado - ID: " + id);
        }
        try {
            clientRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integridade violada");
        }
    }

    public List<String> customerList() {
        List<Client> clients = clientRepository.findAll();

        return clients.stream()
                .map(c -> String.format(
                        "Cliente - Código: %d  - Nome: %s - Endereço: %s - Celular: %s",
                        c.getId(),
                        safe(c.getName()),
                        safe(c.getAddress()),
                        safe(c.getPhone())
                ))
                .toList();
    }

    private String safe(String value) {
        return value == null ? "N/A" : value;
    }
}
