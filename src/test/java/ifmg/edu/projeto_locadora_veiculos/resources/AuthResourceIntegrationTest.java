package ifmg.edu.projeto_locadora_veiculos.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifmg.edu.projeto_locadora_veiculos.dto.ClientDTO;
import ifmg.edu.projeto_locadora_veiculos.dto.LoginRequestDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Client;
import ifmg.edu.projeto_locadora_veiculos.entities.Role;
import ifmg.edu.projeto_locadora_veiculos.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AuthResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        
        // Create test user for login
        Client testClient = new Client();
        testClient.setName("Test User");
        testClient.setEmail("test@example.com");
        testClient.setPassword(passwordEncoder.encode("123456"));
        testClient.setPhone("11999999999");
        testClient.setAddress("Test Address");
        testClient.setCity("Test City");
        testClient.setRole(Role.CLIENT);
        clientRepository.save(testClient);
    }

    @Test
    void shouldRegisterNewClientSuccessfully() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("New Client");
        clientDTO.setEmail("newclient@test.com");
        clientDTO.setPassword("123456");
        clientDTO.setPhone("11888888888");
        clientDTO.setAddress("New Address");
        clientDTO.setCity("New City");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Client"))
                .andExpect(jsonPath("$.email").value("newclient@test.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "123456");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithNonExistentUser() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent@test.com", "123456");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldValidateRegistrationWithMissingName() throws Exception {
        ClientDTO invalidClientDTO = new ClientDTO();
        invalidClientDTO.setEmail("test@test.com");
        invalidClientDTO.setPassword("123456");
        invalidClientDTO.setPhone("11999999999");
        invalidClientDTO.setAddress("Test Address");
        invalidClientDTO.setCity("Test City");
        // name is null

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidClientDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateRegistrationWithMissingEmail() throws Exception {
        ClientDTO invalidClientDTO = new ClientDTO();
        invalidClientDTO.setName("Test Name");
        invalidClientDTO.setPassword("123456");
        invalidClientDTO.setPhone("11999999999");
        invalidClientDTO.setAddress("Test Address");
        invalidClientDTO.setCity("Test City");
        // email is null

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidClientDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateRegistrationWithMissingPassword() throws Exception {
        ClientDTO invalidClientDTO = new ClientDTO();
        invalidClientDTO.setName("Test Name");
        invalidClientDTO.setEmail("test@test.com");
        invalidClientDTO.setPhone("11999999999");
        invalidClientDTO.setAddress("Test Address");
        invalidClientDTO.setCity("Test City");
        // password is null

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidClientDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateRegistrationWithEmptyFields() throws Exception {
        ClientDTO invalidClientDTO = new ClientDTO();
        invalidClientDTO.setName("");
        invalidClientDTO.setEmail("");
        invalidClientDTO.setPassword("");
        invalidClientDTO.setPhone("");
        invalidClientDTO.setAddress("");
        invalidClientDTO.setCity("");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidClientDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateLoginWithMissingEmail() throws Exception {
        LoginRequestDTO invalidLogin = new LoginRequestDTO();
        invalidLogin.setPassword("123456");
        // email is null

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateLoginWithMissingPassword() throws Exception {
        LoginRequestDTO invalidLogin = new LoginRequestDTO();
        invalidLogin.setEmail("test@example.com");
        // password is null

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateLoginWithEmptyFields() throws Exception {
        LoginRequestDTO invalidLogin = new LoginRequestDTO("", "");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateInvalidEmailFormat() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Test Client");
        clientDTO.setEmail("invalid-email"); // Invalid email format
        clientDTO.setPassword("123456");
        clientDTO.setPhone("11888888888");
        clientDTO.setAddress("Test Address");
        clientDTO.setCity("Test City");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isBadRequest());
    }
}