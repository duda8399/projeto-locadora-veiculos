package ifmg.edu.projeto_locadora_veiculos.repositories;

import ifmg.edu.projeto_locadora_veiculos.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
