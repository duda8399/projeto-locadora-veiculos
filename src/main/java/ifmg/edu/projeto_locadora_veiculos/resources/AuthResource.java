package ifmg.edu.projeto_locadora_veiculos.resources;

import ifmg.edu.projeto_locadora_veiculos.dto.ClientDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginRequestDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginResponseDTO;
import ifmg.edu.projeto_locadora_veiculos.oauth.JwtService;
import ifmg.edu.projeto_locadora_veiculos.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "API para autenticação de clientes")
public class AuthResource {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientService clientService;

    @Operation(summary = "Registrar novo cliente", description = "Cria um novo cliente e retorna os dados cadastrados.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                            content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
            }
    )
    @PostMapping("/register")
    public ResponseEntity<ClientDTO> register(
            @Parameter(
                    description = "Dados do novo cliente", required = true,
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            )
            @RequestBody ClientDTO dto) {

        ClientDTO newClient = clientService.insert(dto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newClient.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newClient);
    }

    @Operation(summary = "Login", description = "Realiza login e retorna um token JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
            }
    )
    @PostMapping("/login")
    public LoginResponseDTO login(
            @Parameter(
                    description = "Credenciais de login",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDTO.class))
            )
            @RequestBody LoginRequestDTO loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            log.info("Login bem-sucedido para o e-mail: {}", loginRequest.getEmail());
            return new LoginResponseDTO(token);
        } catch (Exception e) {
            log.error("Erro ao autenticar {}: {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        }
    }
}
