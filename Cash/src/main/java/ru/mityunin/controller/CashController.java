package ru.mityunin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.service.CashService;

@Controller
@RequestMapping("/api")
public class CashController {
    private static final Logger log = LoggerFactory.getLogger(CashController.class);
    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/processOperation")
    public ResponseEntity<ApiResponse<Void>> processOperation(@RequestBody CashOperationRequestDto cashOperationRequestDto) {
        log.info("CashController: process operation {}", cashOperationRequestDto);
        ApiResponse<Void> apiResponse = cashService.processOperation(cashOperationRequestDto);
        if (apiResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }
}
