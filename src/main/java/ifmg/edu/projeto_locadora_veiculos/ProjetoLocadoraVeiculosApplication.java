package ifmg.edu.projeto_locadora_veiculos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(info = @Info(
		title = "Locadora UAI",
		version = "1.0",
		description = "Documentação da API da Locadora de Veículos UAI"
))
@SpringBootApplication
public class ProjetoLocadoraVeiculosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoLocadoraVeiculosApplication.class, args);
	}

}
