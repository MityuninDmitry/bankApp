package ru.mityunin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.model.CashOperation;
import ru.mityunin.service.CashService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CashControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CashService cashService;

    @InjectMocks
    private CashController cashController;

    private ObjectMapper objectMapper;
    private CashOperationRequestDto validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cashController).build();
        objectMapper = new ObjectMapper();

        validRequest = new CashOperationRequestDto();
        validRequest.setAccountNumber("1234567890");
        validRequest.setAction(CashOperation.DEPOSIT);
        validRequest.setMoney(new BigDecimal("100.50"));
        validRequest.setLogin("testUser");
    }

    @Test
    void processOperation_WhenSuccess_ShouldReturnOk() throws Exception {
        // Arrange
        ApiResponse<Void> successResponse = ApiResponse.success("Operation completed successfully");
        when(cashService.processOperation(any(CashOperationRequestDto.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/processOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Operation completed successfully"));
    }

    @Test
    void processOperation_WhenFailure_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ApiResponse<Void> errorResponse = ApiResponse.error("Operation failed");
        when(cashService.processOperation(any(CashOperationRequestDto.class)))
                .thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(post("/api/processOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operation failed"));
    }

    @Test
    void processOperation_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CashOperationRequestDto invalidRequest = new CashOperationRequestDto();
        invalidRequest.setAccountNumber(""); // Blank account number
        invalidRequest.setAction(null); // Null action
        invalidRequest.setMoney(null); // Null money
        invalidRequest.setLogin(null); // Null login

        // Act & Assert - Spring Validation will handle this before service call
        mockMvc.perform(post("/api/processOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processOperation_WhenNegativeMoney_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CashOperationRequestDto invalidMoneyRequest = new CashOperationRequestDto();
        invalidMoneyRequest.setAccountNumber("1234567890");
        invalidMoneyRequest.setAction(CashOperation.DEPOSIT);
        invalidMoneyRequest.setMoney(new BigDecimal("-100.50")); // Negative money
        invalidMoneyRequest.setLogin("testUser");

        // Act & Assert
        mockMvc.perform(post("/api/processOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMoneyRequest)))
                .andExpect(status().isBadRequest());
    }
}