package ru.mityunin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.ExchangeCurrencyFrontUIDto;
import ru.mityunin.service.ExchangeService;

import java.util.List;

@Controller
@RequestMapping("/api")
public class ExchangeController {
    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/currencies")
    public ResponseEntity<ApiResponse<List<ExchangeCurrencyDto>>> actualCurrencies() {
        ApiResponse<List<ExchangeCurrencyDto>> apiResponse = exchangeService.actualCurrencies();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/currenciesFrontUI")
    public ResponseEntity<ApiResponse<List<ExchangeCurrencyFrontUIDto>>> actualCurrenciesFrontUI() {
        ApiResponse<List<ExchangeCurrencyFrontUIDto>> apiResponse = exchangeService.actualCurrenciesFrontUI();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
