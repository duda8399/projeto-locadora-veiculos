package ifmg.edu.projeto_locadora_veiculos.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginRequestDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginResponseDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Client;
import ifmg.edu.projeto_locadora_veiculos.entities.Reservation;
import ifmg.edu.projeto_locadora_veiculos.entities.Role;
import ifmg.edu.projeto_locadora_veiculos.entities.Vehicle;
import ifmg.edu.projeto_locadora_veiculos.repositories.ClientRepository;
import ifmg.edu.projeto_locadora_veiculos.repositories.ReservationRepository;
import ifmg.edu.projeto_locadora_veiculos.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ReportResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String clientToken;
    private Client testClient;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();

        // Create admin user and get token
        Client adminClient = new Client();
        adminClient.setName("Admin Report");
        adminClient.setEmail("admin.report@test.com");
        adminClient.setPassword(passwordEncoder.encode("123456"));
        adminClient.setRole(Role.ADMIN);
        clientRepository.save(adminClient);
        adminToken = loginAndGetToken("admin.report@test.com", "123456");

        // Create regular client and get token
        testClient = new Client();
        testClient.setName("Client Report");
        testClient.setEmail("client.report@test.com");
        testClient.setPassword(passwordEncoder.encode("123456"));
        testClient.setAddress("123 Report St");
        testClient.setCity("Reportville");
        testClient.setRole(Role.CLIENT);
        testClient = clientRepository.save(testClient);
        clientToken = loginAndGetToken("client.report@test.com", "123456");

        // Create a vehicle
        testVehicle = new Vehicle();
        testVehicle.setPlate("REP-001");
        testVehicle.setBrand("Report Brand");
        testVehicle.setModel("Report Model");
        testVehicle.setYear("2024");
        testVehicle.setDailyValue(100.0);
        testVehicle.setDescription("A vehicle for reports");
        testVehicle = vehicleRepository.save(testVehicle);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        LoginResponseDTO loginResponse = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponseDTO.class);
        return loginResponse.getToken();
    }

    private void cleanDatabase() {
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldGetClientReportAsAdmin() throws Exception {
        mockMvc.perform(get("/report/clients")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cliente - Código:")))
                .andExpect(content().string(containsString("Admin Report")))
                .andExpect(content().string(containsString("Client Report")));
    }

    @Test
    void shouldGetVehicleReportAsAdmin() throws Exception {
        mockMvc.perform(get("/report/vehicles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carro - Placa: REP-001")));
    }

    @Test
    void shouldGenerateInvoiceForClient() throws Exception {
        Reservation reservation = new Reservation(testClient, testVehicle, Instant.now().minus(5, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));
        reservationRepository.save(reservation);

        mockMvc.perform(get("/report/invoice/{clientId}", testClient.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("NOTA FISCAL")))
                .andExpect(content().string(containsString("Nome: Client Report")))
                .andExpect(content().string(containsString("Total geral: R$ 500,00")));
    }

    @Test
    void shouldGetActiveReservationsReport() throws Exception {
        Reservation activeReservation = new Reservation(testClient, testVehicle, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(3, ChronoUnit.DAYS));
        reservationRepository.save(activeReservation);

        mockMvc.perform(get("/report/reservations/active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reserva ativa - Cliente: Client Report")));
    }

    @Test
    void shouldGetRevenueReport() throws Exception {
        Reservation reservation = new Reservation(testClient, testVehicle, Instant.parse("2024-07-10T00:00:00Z"), Instant.parse("2024-07-15T00:00:00Z"));
        reservationRepository.save(reservation); // 6 days * 100 = 600

        mockMvc.perform(get("/report/revenue")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("start", "01-07-2024")
                        .param("end", "31-07-2024"))
                .andExpect(status().isOk())
                .andExpect(content().string("Faturamento do período de 01/07/2024 à 31/07/2024: R$ 600,00"));
    }

    @Test
    void shouldReturnForbiddenForClientAccessingAdminReport() throws Exception {
        mockMvc.perform(get("/report/clients")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }
}