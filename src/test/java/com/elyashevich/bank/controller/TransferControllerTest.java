package com.elyashevich.bank.controller;

import com.elyashevich.bank.api.dto.auth.LoginDto;
import com.elyashevich.bank.api.dto.transfer.TransferDto;
import com.elyashevich.bank.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@Import(TestConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TransferControllerTest {

    public static final String URL = "/api/v1/transfer";
    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static TransferDto validTransferDto;
    private static TransferDto sameUserTransferDto;
    private static TransferDto invalidAmountTransferDto;
    private static TransferDto insufficientFundsTransferDto;
    private static TransferDto maxBalanceExceededTransferDto;

    @BeforeAll
    static void setup() {
        validTransferDto = new TransferDto(2L, BigDecimal.valueOf(100));
        sameUserTransferDto = new TransferDto(1L, BigDecimal.valueOf(100));
        invalidAmountTransferDto = new TransferDto(2L, BigDecimal.ZERO);
        insufficientFundsTransferDto = new TransferDto(2L, BigDecimal.valueOf(10000));
        maxBalanceExceededTransferDto = new TransferDto(2L, BigDecimal.valueOf(1000000));
    }

    @Test
    void testTransferMoneySuccess() throws Exception {
        var accessToken = getAccessTokenFromRequest("user@example.com", "password");

        var request = MockMvcRequestBuilders.post(URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    void testTransferToSameUser() throws Exception {
        var accessToken = getAccessTokenFromRequest("user@example.com", "password");

        var request = MockMvcRequestBuilders.post(URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sameUserTransferDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.code", is(400)));
    }

    @Test
    void testTransferWithInvalidAmount() throws Exception {
        var accessToken = getAccessTokenFromRequest("user@example.com", "password");

        var request = MockMvcRequestBuilders.post(URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAmountTransferDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.errors.amount", notNullValue()))
                .andExpect(jsonPath("$.code", is(400)));
    }

    @Test
    void testTransferWithInsufficientFunds() throws Exception {
        var accessToken = getAccessTokenFromRequest("user@example.com", "password");

        var request = MockMvcRequestBuilders.post(URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(insufficientFundsTransferDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.code", is(403)));
    }

    @Test
    void testTransferExceedingMaxBalance() throws Exception {
        var accessToken = getAccessTokenFromRequest("user@example.com", "password");
        var request = MockMvcRequestBuilders.post(URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxBalanceExceededTransferDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.code", is(403)));
    }

    @Test
    void testTransferWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    String getAccessTokenFromRequest(String email, String password) throws Exception {
        var loginRequest = new LoginDto(email, password);

        var responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseContent)
                .path("accessToken")
                .asText();
    }
}