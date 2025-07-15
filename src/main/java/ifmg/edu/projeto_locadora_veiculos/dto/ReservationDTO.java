package ifmg.edu.projeto_locadora_veiculos.dto;

import ifmg.edu.projeto_locadora_veiculos.entities.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.Objects;

public class ReservationDTO extends RepresentationModel<ReservationDTO> {

    @Schema(description = "ID da reserva gerado pelo banco de dados")
    private long id;

    @Schema(description = "ID do cliente associado à reserva", example = "1", required = true)
    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clientId;

    @Schema(description = "ID do veículo associado à reserva", example = "10", required = true)
    @NotNull(message = "O ID do veículo é obrigatório")
    private Long vehicleId;

    @Schema(description = "Data de início da reserva")
    @NotNull(message = "A data de início é obrigatória")
    private Instant startDate;

    @Schema(description = "Data de término da reserva")
    @NotNull(message = "A data de término é obrigatória")
    private Instant endDate;

    public ReservationDTO() {}

    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.clientId = reservation.getClient().getId();
        this.vehicleId = reservation.getVehicle().getId();
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReservationDTO dto)) return false;
        return id == dto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ReservationDTO{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", vehicleId=" + vehicleId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}