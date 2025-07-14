package ifmg.edu.projeto_locadora_veiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de resposta para autenticação bem-sucedida. Contém o token JWT gerado.")
public class LoginResponseDTO {

    @Schema(
            description = "Token JWT gerado após login bem-sucedido. Deve ser utilizado para autenticação nas próximas requisições.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
