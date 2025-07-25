package ifmg.edu.projeto_locadora_veiculos.repositories;

import ifmg.edu.projeto_locadora_veiculos.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
}
