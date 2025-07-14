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

    @Schema(description = "Cliente que realizou a reserva")
    @NotNull(message = "O cliente é obrigatório")
    private ClientDTO client;

    @Schema(description = "Veículo reservado")
    @NotNull(message = "O veículo é obrigatório")
    private VehicleDTO vehicle;

    @Schema(description = "Data de início da reserva")
    @NotNull(message = "A data de início é obrigatória")
    private Instant startDate;

    @Schema(description = "Data de término da reserva")
    @NotNull(message = "A data de término é obrigatória")
    private Instant endDate;

    public ReservationDTO() {}

    public ReservationDTO(ClientDTO client, VehicleDTO vehicle, Instant startDate, Instant endDate) {
        this.client = client;
        this.vehicle = vehicle;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.client = new ClientDTO(reservation.getClient());
        this.vehicle = new VehicleDTO(reservation.getVehicle());
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    public VehicleDTO getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleDTO vehicle) {
        this.vehicle = vehicle;
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
                ", client=" + client +
                ", vehicle=" + vehicle +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}