package ifmg.edu.projeto_locadora_veiculos.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifmg.edu.projeto_locadora_veiculos.dto.ClientDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginRequestDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginResponseDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Client;
import ifmg.edu.projeto_locadora_veiculos.entities.Role;
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
public class ClientResourceIntegrationTest {

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
    private Client adminClient;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up in the correct order to avoid foreign key constraint violations
        cleanDatabase();
        
        // Create admin user
        adminClient = new Client();
        adminClient.setName("Admin Test");
        adminClient.setEmail("admin@test.com");
        adminClient.setPassword(passwordEncoder.encode("123456"));
        adminClient.setPhone("11999999999");
        adminClient.setAddress("Test Address");
        adminClient.setCity("Test City");
        adminClient.setRole(Role.ADMIN);
        adminClient = clientRepository.save(adminClient);

        // Get admin token with debugging
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin@test.com", "123456");
        
        try {
            MvcResult result = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(mvcResult -> {
                        System.out.println("Login response status: " + mvcResult.getResponse().getStatus());
                        System.out.println("Login response body: " + mvcResult.getResponse().getContentAsString());
                    })
                    .andExpect(status().isOk())
                    .andReturn();
                    
            LoginResponseDTO loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                LoginResponseDTO.class
            );
            adminToken = loginResponse.getToken();
            System.out.println("Admin token: " + adminToken);
        } catch (Exception e) {
            System.err.println("Failed to get admin token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void cleanDatabase() {
        try {
            reservationRepository.deleteAll();
            vehicleRepository.deleteAll();
            clientRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Warning during cleanup: " + e.getMessage());
        }
    }

    @Test
    void shouldGetAllClientsWithPagination() throws Exception {
        // Create test clients
        for (int i = 1; i <= 5; i++) {
            Client client = new Client();
            client.setName("Client " + i);
            client.setEmail("client" + i + "@test.com");
            client.setPassword(passwordEncoder.encode("123456"));
            client.setPhone("1199999999" + i);
            client.setAddress("Address " + i);
            client.setCity("City " + i);
            client.setRole(Role.CLIENT);
            clientRepository.save(client);
        }

        mockMvc.perform(get("/client")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "3"))
                .andDo(result -> {
                    System.out.println("Get clients response status: " + result.getResponse().getStatus());
                    System.out.println("Get clients response body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(6)) // 5 created + 1 admin
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldCreateClientSuccessfully() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Test Client");
        clientDTO.setEmail("client@test.com");
        clientDTO.setPassword("123456");
        clientDTO.setPhone("11888888888");
        clientDTO.setAddress("Client Address");
        clientDTO.setCity("Client City");

        mockMvc.perform(post("/client")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
                .andDo(result -> {
                    System.out.println("Create client response status: " + result.getResponse().getStatus());
                    System.out.println("Create client response body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Cliente criado com sucesso!"))
                .andExpect(jsonPath("$.data.name").value("Test Client"))
                .andExpect(jsonPath("$.data.email").value("client@test.com"));
    }

    @Test
    void shouldGetClientById() throws Exception {
        mockMvc.perform(get("/client/{id}", adminClient.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andDo(result -> {
                    System.out.println("Get client by ID response status: " + result.getResponse().getStatus());
                    System.out.println("Get client by ID response body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Test"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void shouldUpdateClient() throws Exception {
        ClientDTO updateDTO = new ClientDTO();
        updateDTO.setName("Updated Admin");
        updateDTO.setEmail("admin@test.com");
        updateDTO.setPhone("11777777777");
        updateDTO.setAddress("Updated Address");
        updateDTO.setCity("Updated City");

        mockMvc.perform(put("/client/{id}", adminClient.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(result -> {
                    System.out.println("Update client response status: " + result.getResponse().getStatus());
                    System.out.println("Update client response body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cliente atualizado com sucesso!"))
                .andExpect(jsonPath("$.data.name").value("Updated Admin"))
                .andExpect(jsonPath("$.data.phone").value("11777777777"));
    }

    @Test
    void shouldDeleteClient() throws Exception {
        // Create a client to delete
        Client clientToDelete = new Client();
        clientToDelete.setName("Delete Me");
        clientToDelete.setEmail("delete@test.com");
        clientToDelete.setPassword(passwordEncoder.encode("123456"));
        clientToDelete.setPhone("11666666666");
        clientToDelete.setAddress("Delete Address");
        clientToDelete.setCity("Delete City");
        clientToDelete.setRole(Role.CLIENT);
        clientToDelete = clientRepository.save(clientToDelete);

        mockMvc.perform(delete("/client/{id}", clientToDelete.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andDo(result -> {
                    System.out.println("Delete client response status: " + result.getResponse().getStatus());
                    System.out.println("Delete client response body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(content().string("Cliente deletado com sucesso."));
    }

    @Test
    void shouldReturnNotFoundForNonExistentClient() throws Exception {
        mockMvc.perform(get("/client/{id}", 99999L)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/client"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debugTokenValidation() throws Exception {
        System.out.println("=== DEBUG TOKEN VALIDATION ===");
        System.out.println("Admin token: " + adminToken);
        System.out.println("Admin client ID: " + adminClient.getId());
        System.out.println("Admin client email: " + adminClient.getEmail());
        System.out.println("Admin client role: " + adminClient.getRole());
        
        // Test a simple GET request with debug info
        mockMvc.perform(get("/client")
                .header("Authorization", "Bearer " + adminToken))
                .andDo(result -> {
                    System.out.println("Debug response status: " + result.getResponse().getStatus());
                    System.out.println("Debug response headers: " + result.getResponse().getHeaderNames());
                    System.out.println("Debug response body: " + result.getResponse().getContentAsString());
                });
    }
}