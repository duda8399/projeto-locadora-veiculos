package ifmg.edu.projeto_locadora_veiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO utilizado para realizar login de um cliente no sistema.")
public class LoginRequestDTO {

    @Schema(description = "E-mail utilizado para login", example = "usuario@example.com", required = true)
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @Schema(description = "Senha do usuário", example = "123456", required = true)
    @NotBlank(message = "A senha é obrigatória")
    private String password;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
