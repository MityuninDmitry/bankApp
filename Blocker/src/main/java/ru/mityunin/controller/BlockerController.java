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
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.service.BlockerService;

@Controller
@RequestMapping("/blocker")
public class BlockerController {

    private static final Logger log = LoggerFactory.getLogger(BlockerController.class);
    private final BlockerService blockerService;

    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }


    @PostMapping("/checkOperation")
    public ResponseEntity<ApiResponse<Void>> processOperation(@RequestBody CashOperationRequest cashOperationRequest) {
        log.info("CashController: process operation {}", cashOperationRequest);
        ApiResponse<Void> apiResponse = blockerService.isSuspiciousOperation(cashOperationRequest);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

}
