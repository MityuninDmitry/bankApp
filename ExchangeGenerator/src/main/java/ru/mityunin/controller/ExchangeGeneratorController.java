package ru.mityunin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.model.ExchangeCurrency;
import ru.mityunin.service.ExchangeGeneratorService;

import java.util.List;

@Controller
@RequestMapping("/exchangegenerator")
public class ExchangeGeneratorController {

    private static final Logger log = LoggerFactory.getLogger(ExchangeGeneratorController.class);
    private final ExchangeGeneratorService generatorService;

    public ExchangeGeneratorController(ExchangeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @GetMapping("/currencies")
    public ResponseEntity<ApiResponse<List<ExchangeCurrencyDto>>> actualCurrencies() {
        ApiResponse<List<ExchangeCurrencyDto>> apiResponse = generatorService.actualCurrencies();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
