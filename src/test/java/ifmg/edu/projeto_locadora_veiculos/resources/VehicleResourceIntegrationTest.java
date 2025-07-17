package ifmg.edu.projeto_locadora_veiculos.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginRequestDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginResponseDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.VehicleDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Client;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VehicleResourceIntegrationTest {

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

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();

        // Create admin user and get token
        Client adminClient = new Client();
        adminClient.setName("Admin Vehicle");
        adminClient.setEmail("admin.vehicle@test.com");
        adminClient.setPassword(passwordEncoder.encode("123456"));
        adminClient.setRole(Role.ADMIN);
        clientRepository.save(adminClient);
        adminToken = loginAndGetToken("admin.vehicle@test.com", "123456");

        // Create regular client and get token
        Client client = new Client();
        client.setName("Client Vehicle");
        client.setEmail("client.vehicle@test.com");
        client.setPassword(passwordEncoder.encode("123456"));
        client.setRole(Role.CLIENT);
        clientRepository.save(client);
        clientToken = loginAndGetToken("client.vehicle@test.com", "123456");
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
    void shouldGetAllVehiclesWithPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setPlate("PLT-000" + i);
            vehicle.setBrand("Brand " + i);
            vehicle.setModel("Model " + i);
            vehicle.setYear("202" + i);
            vehicleRepository.save(vehicle);
        }

        mockMvc.perform(get("/vehicle")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldCreateVehicleSuccessfullyAsAdmin() throws Exception {
        VehicleDTO vehicleDTO = new VehicleDTO("NEW-001", "New Brand", "New Model", "2025", "Red", "Desc", "", 250.0);

        mockMvc.perform(post("/vehicle")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Veículo criado com sucesso!"))
                .andExpect(jsonPath("$.data.plate").value("NEW-001"));
    }

    @Test
    void shouldReturnForbiddenWhenCreatingVehicleAsClient() throws Exception {
        VehicleDTO vehicleDTO = new VehicleDTO("FAIL-001", "Fail Brand", "Fail Model", "2025", "Red", "Desc", "", 250.0);

        mockMvc.perform(post("/vehicle")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetVehicleById() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate("GET-001");
        vehicle.setBrand("Get Brand");
        vehicle.setModel("Get Model");
        vehicle.setYear("2024");
        vehicle = vehicleRepository.save(vehicle);

        mockMvc.perform(get("/vehicle/{id}", vehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("GET-001"));
    }

    @Test
    void shouldUpdateVehicleAsAdmin() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate("UPD-001");
        vehicle.setBrand("Update Brand");
        vehicle.setModel("Update Model");
        vehicle.setYear("2023");
        vehicle = vehicleRepository.save(vehicle);

        VehicleDTO updateDTO = new VehicleDTO("UPD-002", "Updated Brand", "Updated Model", "2024", "Blue", "New Desc", "", 300.0);

        mockMvc.perform(put("/vehicle/{id}", vehicle.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Veículo atualizado com sucesso!"))
                .andExpect(jsonPath("$.data.plate").value("UPD-002"))
                .andExpect(jsonPath("$.data.year").value("2024"));
    }

    @Test
    void shouldDeleteVehicleAsAdmin() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate("DEL-001");
        vehicle.setBrand("Delete Brand");
        vehicle.setModel("Delete Model");
        vehicle.setYear("2022");
        vehicle = vehicleRepository.save(vehicle);

        mockMvc.perform(delete("/vehicle/{id}", vehicle.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Veículo deletado com sucesso."));
    }

    @Test
    void shouldReturnNotFoundForNonExistentVehicle() throws Exception {
        mockMvc.perform(get("/vehicle/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedForPostWithoutToken() throws Exception {
        VehicleDTO vehicleDTO = new VehicleDTO("UNAUTH-001", "Unauth Brand", "Unauth Model", "2025", "Red", "Desc", "", 250.0);
        mockMvc.perform(post("/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDTO)))
                .andExpect(status().isUnauthorized());
    }
}