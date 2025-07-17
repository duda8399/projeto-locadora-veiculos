package ifmg.edu.projeto_locadora_veiculos.repositories;

import ifmg.edu.projeto_locadora_veiculos.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.vehicle.id = :vehicleId
          AND r.startDate < :endDate
          AND r.endDate > :startDate
    """)
    boolean existsByVehicleIdAndDateRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    List<Reservation> findByClientId(Long clientId);
}

