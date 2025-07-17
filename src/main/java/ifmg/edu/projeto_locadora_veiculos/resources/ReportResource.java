package ifmg.edu.projeto_locadora_veiculos.resources;

import ifmg.edu.projeto_locadora_veiculos.services.ClientService;
import ifmg.edu.projeto_locadora_veiculos.services.ReservationService;
import ifmg.edu.projeto_locadora_veiculos.services.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(value = "/report")
@Tag(name = "Relatórios", description = "API para consulta de relatórios")
public class ReportResource {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private VehicleService vehicleService;

    @Operation(
            summary = "Relatório de clientes",
            description = "Lista formatada com todos os clientes cadastrados. Requer permissão ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum cliente encontrado", content = @Content)
            }
    )
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getClientReport() {
        List<String> report = clientService.customerList();
        return report.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Relatório de veículos",
            description = "Lista formatada com todos os veículos cadastrados.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum veículo encontrado", content = @Content)
            }
    )
    @GetMapping("/vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getVehicleReport() {
        List<String> report = vehicleService.vehicleList();
        return report.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Relatório de reservas",
            description = "Lista formatada com todas as reservas cadastradas. Requer permissão ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhuma reserva encontrada", content = @Content)
            }
    )
    @GetMapping("/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getReservationReport() {
        List<String> report = reservationService.reservationList();
        return report.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Gerar nota fiscal do cliente",
            description = "Gera um cupom fiscal com o total de reservas de um cliente. Requer permissão ADMIN/CLIENT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Nota fiscal gerada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao gerar nota fiscal", content = @Content)
            }
    )
    @GetMapping("/invoice/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<String> generateInvoice(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long clientId) {
        try {
            String taxCoupon = reservationService.generateInvoice(clientId);
            return ResponseEntity.ok(taxCoupon);
        } catch (IllegalArgumentException | ResponseStatusException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @Operation(summary = "Relatório de reservas ativas", description = "Lista todas as reservas ativas hoje.")
    @GetMapping("/reservations/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getActiveReservationsReport() {
        List<String> report = reservationService.activeReservationsReport();
        return report.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(report);
    }

    @Operation(summary = "Relatório de reservas por veículo", description = "Mostra quantas vezes cada veículo foi reservado.")
    @GetMapping("/reservations/per-vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getReservationsPerVehicleReport() {
        List<String> report = reservationService.reservationsPerVehicleReport();
        return report.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(report);
    }


    @Operation(
            summary = "Calcular faturamento por período",
            description = "Retorna o valor total faturado entre duas datas, com base nas reservas registradas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Faturamento retornado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
            }
    )
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getRevenue(
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDateTime startDate = LocalDate.parse(start, inputFormatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(end, inputFormatter).atTime(23, 59, 59);

            Instant startInstant = startDate.toInstant(ZoneOffset.UTC);
            Instant endInstant = endDate.toInstant(ZoneOffset.UTC);

            double revenue = reservationService.calculateRevenueByPeriod(startInstant, endInstant);

            String response = String.format(
                    "Faturamento do período de %s à %s: R$ %.2f",
                    outputFormatter.format(startDate),
                    outputFormatter.format(endDate),
                    revenue
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Parâmetros de data inválidos. Use o formato yyyy-MM-dd.");
        }
    }

}
