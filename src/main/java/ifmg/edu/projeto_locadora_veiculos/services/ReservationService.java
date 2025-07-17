package ifmg.edu.projeto_locadora_veiculos.services;

import ifmg.edu.projeto_locadora_veiculos.dto.ReservationDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Client;
import ifmg.edu.projeto_locadora_veiculos.entities.Reservation;
import ifmg.edu.projeto_locadora_veiculos.entities.Vehicle;
import ifmg.edu.projeto_locadora_veiculos.repositories.ClientRepository;
import ifmg.edu.projeto_locadora_veiculos.repositories.ReservationRepository;
import ifmg.edu.projeto_locadora_veiculos.repositories.VehicleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping(produces = "application/json")
    @Operation(
            description = "Obtenha todas as reservas",
            summary = "Listar todas as reservas cadastradas",
            responses = {
                    @ApiResponse(description = "ok", responseCode = "200"),
            }
    )
    public Page<ReservationDTO> findAll(Pageable pageable) {
        Page<Reservation> page = reservationRepository.findAll(pageable);
        return page.map(ReservationDTO::new);
    }

    @Transactional(readOnly = true)
    public ReservationDTO findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada"));
        return new ReservationDTO(reservation);
    }

    @Transactional
    public ReservationDTO insert(ReservationDTO dto) {
        boolean exists = reservationRepository.existsByVehicleIdAndDateRange(
                dto.getVehicleId(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma reserva nesse período");
        }

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));

        Reservation reservation = new Reservation(
                client,
                vehicle,
                dto.getStartDate(),
                dto.getEndDate()
        );

        reservation = reservationRepository.save(reservation);
        return new ReservationDTO(reservation);
    }

    @Transactional
    public ReservationDTO update(Long id, ReservationDTO dto) {
        try {
            boolean exists = reservationRepository.existsByVehicleIdAndDateRange(
                    dto.getVehicleId(),
                    dto.getStartDate(),
                    dto.getEndDate()
            );

            if (exists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma reserva nesse período");
            }

            Reservation reservation = reservationRepository.getReferenceById(id);

            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));

            reservation.setClient(client);
            reservation.setVehicle(vehicle);
            reservation.setStartDate(dto.getStartDate());
            reservation.setEndDate(dto.getEndDate());

            return new ReservationDTO(reservationRepository.save(reservation));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada");
        }
    }

    public void delete(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada");
        }
        reservationRepository.deleteById(id);
    }

    public List<String> reservationList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneOffset.UTC);

        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .map(r -> {
                    Instant startInstant = r.getStartDate();
                    Instant endInstant = r.getEndDate();

                    return String.format("Reserva: Cliente: %s - Veículo: %s %s %s - Período: %s à %s",
                            r.getClient().getName(),
                            r.getVehicle().getModel(),
                            r.getVehicle().getBrand(),
                            r.getVehicle().getYear(),
                            formatter.format(startInstant),
                            formatter.format(endInstant));
                })
                .toList();
    }

    public String generateInvoice(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        if (isNullOrEmpty(client.getName()) ||
                isNullOrEmpty(client.getAddress()) ||
                isNullOrEmpty(client.getCity())) {
            throw new IllegalArgumentException("Todos os dados do cliente devem ser preenchidos.");
        }

        List<Reservation> reservations = reservationRepository.findByClientId(clientId);

        List<Reservation> validReservations = reservations.stream()
                .filter(r -> r.getVehicle() != null &&
                        r.getVehicle().getDescription() != null &&
                        !r.getVehicle().getDescription().isBlank() &&
                        r.getVehicle().getDailyValue() != 0)
                .toList();

        if (validReservations.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma estadia com descrição e valor informados.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===============================\n");
        sb.append("         NOTA FISCAL\n");
        sb.append("===============================\n");
        sb.append("Nome: ").append(client.getName()).append("\n");
        sb.append("Endereço: ").append(client.getAddress()).append("\n");
        sb.append("Cidade: ").append(client.getCity()).append("\n");
        sb.append("===============================\n");
        sb.append("        ==== VEÍCULOS ====\n");

        double total = 0.0;
        for (Reservation reservation : validReservations) {
            long days = ChronoUnit.DAYS.between(
                    reservation.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                    reservation.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate()) + 1;

            double dailyValue = reservation.getVehicle().getDailyValue();
            double reservationTotal = days * dailyValue;

            sb.append("Veículo: ").append(reservation.getVehicle().getModel())
                    .append(" | Diárias: ").append(days)
                    .append(" | Valor diário: R$ ").append(String.format("%.2f", dailyValue))
                    .append(" | Total: R$ ").append(String.format("%.2f", reservationTotal))
                    .append("\n");

            total += reservationTotal;
        }

        sb.append("===============================\n");
        sb.append("Total geral: R$ ").append(String.format("%.2f", total)).append("\n");
        sb.append("===============================");

        return sb.toString();
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isBlank();
    }

    public List<String> activeReservationsReport() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneOffset.UTC);
        Instant now = Instant.now();

        List<Reservation> active = reservationRepository.findAll().stream()
                .filter(r -> {
                    Instant start = r.getStartDate();
                    Instant end = r.getEndDate();
                    return !now.isBefore(start) && !now.isAfter(end);
                })
                .toList();

        return active.stream()
                .map(r -> String.format(
                        "Reserva ativa - Cliente: %s | Veículo: %s %s %s | Período: %s à %s",
                        r.getClient().getName(),
                        r.getVehicle().getBrand(),
                        r.getVehicle().getModel(),
                        r.getVehicle().getYear(),
                        formatter.format(r.getStartDate()),
                        formatter.format(r.getEndDate())))
                .toList();
    }

    public List<String> reservationsPerVehicleReport() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                r -> String.format("%s %s %s",
                                        r.getVehicle().getBrand(),
                                        r.getVehicle().getModel(),
                                        r.getVehicle().getYear()
                                ),
                                java.util.stream.Collectors.counting()
                        )
                )
                .entrySet().stream()
                .map(entry -> String.format("Veículo: %s | Total de reservas: %d", entry.getKey(), entry.getValue()))
                .toList();
    }

    public double calculateRevenueByPeriod(Instant startPeriod, Instant endPeriod) {
        if (startPeriod == null || endPeriod == null || endPeriod.isBefore(startPeriod)) {
            throw new IllegalArgumentException("Período inválido.");
        }

        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> {
                    Instant start = r.getStartDate();
                    Instant end = r.getEndDate();
                    return !(end.isBefore(startPeriod) || start.isAfter(endPeriod));
                })
                .toList();

        double totalRevenue = 0.0;

        for (Reservation reservation : reservations) {
            Vehicle vehicle = reservation.getVehicle();
            if (vehicle != null && vehicle.getDailyValue() > 0) {
                Instant effectiveStart = reservation.getStartDate().isBefore(startPeriod) ? startPeriod : reservation.getStartDate();
                Instant effectiveEnd = reservation.getEndDate().isAfter(endPeriod) ? endPeriod : reservation.getEndDate();

                long days = Math.max(1,
                        java.time.Duration.between(effectiveStart, effectiveEnd).toDays() + 1);

                totalRevenue += days * vehicle.getDailyValue();
            }
        }

        return totalRevenue;
    }

}
