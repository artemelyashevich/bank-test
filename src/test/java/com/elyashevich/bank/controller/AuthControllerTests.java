package com.elyashevich.bank.controller;

import com.elyashevich.bank.api.dto.auth.JwtResponse;
import com.elyashevich.bank.api.dto.auth.LoginDto;
import com.elyashevich.bank.api.dto.auth.RegisterDto;
import com.elyashevich.bank.api.mapper.UserMapper;
import com.elyashevich.bank.config.TestConfig;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static RegisterDto validRegisterDto;
    private static RegisterDto invalidRegisterDto;
    private static RegisterDto duplicateRegisterDto;
    private static LoginDto validLoginDto;
    private static LoginDto invalidLoginDto;
    private static LoginDto badCredentialLoginDto;
    private static JwtResponse jwtResponse;

    @BeforeAll
    static void setup() {
        // Register DTOs
        validRegisterDto = new RegisterDto(
                "user@example.com",
                "John Doe",
                LocalDate.of(1990, 1, 15),
                "securePassword123",
                "79123456789",
                new BigDecimal("1000.00")
        );

        invalidRegisterDto = new RegisterDto(
                "invalid-email",
                "",
                null,
                "short",
                "12345678901",
                new BigDecimal("-100")
        );

        duplicateRegisterDto = new RegisterDto(
                "existing@example.com",
                "John Doe",
                LocalDate.of(1990, 1, 15),
                "securePassword123",
                "79123456789",
                new BigDecimal("1000.00")
        );

        // Login DTOs
        validLoginDto = new LoginDto(
                "user@example.com",
                "securePassword123"
        );

        invalidLoginDto = new LoginDto(
                "invalid-email",
                "short"
        );

        badCredentialLoginDto = new LoginDto(
                "nonexistent@example.com",
                "wrongpassword"
        );

        // JWT Response
        jwtResponse = new JwtResponse("test-access-token", "test-refresh-token");
    }

    @Test
    @DisplayName("Test successful registration - Integration")
    void testRegisterSuccess() throws Exception {
        when(userMapper.toEntity(validRegisterDto)).thenReturn(new User());
        when(authService.register(any(User.class))).thenReturn(jwtResponse);

        var request = MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test registration validation - Integration")
    void testRegisterValidation() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRegisterDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.dateOfBirth").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.balance").exists());
    }

    @Test
    @DisplayName("Test duplicate registration - Integration")
    void testRegisterDuplicate() throws Exception {
        when(userMapper.toEntity(duplicateRegisterDto)).thenReturn(new User());
        when(authService.register(any(User.class)))
                .thenThrow(new RuntimeException("User with this email already exists"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRegisterDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Test successful login - Integration")
    void testLoginSuccess() throws Exception {
        when(userMapper.toEntity(validLoginDto)).thenReturn(new User());
        when(authService.login(any(User.class))).thenReturn(jwtResponse);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test login validation - Integration")
    void testLoginValidation() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Test login with bad credentials - Integration")
    void testLoginBadCredentials() throws Exception {
        when(userMapper.toEntity(badCredentialLoginDto)).thenReturn(new User());
        when(authService.login(any(User.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badCredentialLoginDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}