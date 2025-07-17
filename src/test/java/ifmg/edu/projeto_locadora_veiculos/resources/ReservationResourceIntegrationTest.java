package ifmg.edu.projeto_locadora_veiculos.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginRequestDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginResponseDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.ReservationDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ReservationResourceIntegrationTest {

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
        adminClient.setName("Admin Reservation");
        adminClient.setEmail("admin.reservation@test.com");
        adminClient.setPassword(passwordEncoder.encode("123456"));
        adminClient.setRole(Role.ADMIN);
        clientRepository.save(adminClient);
        adminToken = loginAndGetToken("admin.reservation@test.com", "123456");

        // Create regular client and get token
        testClient = new Client();
        testClient.setName("Client Reservation");
        testClient.setEmail("client.reservation@test.com");
        testClient.setPassword(passwordEncoder.encode("123456"));
        testClient.setRole(Role.CLIENT);
        testClient = clientRepository.save(testClient);
        clientToken = loginAndGetToken("client.reservation@test.com", "123456");

        // Create a vehicle
        testVehicle = new Vehicle();
        testVehicle.setPlate("RES-001");
        testVehicle.setBrand("Reservation Brand");
        testVehicle.setModel("Reservation Model");
        testVehicle.setYear("2024");
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
    void shouldCreateReservationSuccessfullyAsClient() throws Exception {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setClientId(testClient.getId());
        reservationDTO.setVehicleId(testVehicle.getId());
        reservationDTO.setStartDate(Instant.now().plus(1, ChronoUnit.DAYS));
        reservationDTO.setEndDate(Instant.now().plus(5, ChronoUnit.DAYS));

        mockMvc.perform(post("/reservation")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Reserva criada com sucesso!"))
                .andExpect(jsonPath("$.data.clientId").value(testClient.getId()));
    }

    @Test
    void shouldFailToCreateReservationForOverlappingDates() throws Exception {
        // First reservation
        Reservation reservation = new Reservation(testClient, testVehicle, Instant.now().plus(10, ChronoUnit.DAYS), Instant.now().plus(15, ChronoUnit.DAYS));
        reservationRepository.save(reservation);

        // Attempt to create an overlapping reservation
        ReservationDTO overlappingDTO = new ReservationDTO();
        overlappingDTO.setClientId(testClient.getId());
        overlappingDTO.setVehicleId(testVehicle.getId());
        overlappingDTO.setStartDate(Instant.now().plus(12, ChronoUnit.DAYS));
        overlappingDTO.setEndDate(Instant.now().plus(17, ChronoUnit.DAYS));

        mockMvc.perform(post("/reservation")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetAllReservationsAsAdmin() throws Exception {
        mockMvc.perform(get("/reservation")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnForbiddenWhenClientTriesToGetAllReservations() throws Exception {
        mockMvc.perform(get("/reservation")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteReservationAsAdmin() throws Exception {
        Reservation reservation = new Reservation(testClient, testVehicle, Instant.now().plus(20, ChronoUnit.DAYS), Instant.now().plus(25, ChronoUnit.DAYS));
        reservation = reservationRepository.save(reservation);

        mockMvc.perform(delete("/reservation/{id}", reservation.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva deletada com sucesso!"));
    }
}