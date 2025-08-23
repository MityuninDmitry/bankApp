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
import ru.mityunin.service.BlockerService;
import ru.mityunin.service.NotificationService;

@Controller
@RequestMapping("/blocker")
public class BlockerController {

    private static final Logger log = LoggerFactory.getLogger(BlockerController.class);
    private final BlockerService blockerService;
    private final NotificationService notificationService;

    public BlockerController(BlockerService blockerService, NotificationService notificationService) {
        this.blockerService = blockerService;
        this.notificationService = notificationService;
    }


    @PostMapping("/checkOperation")
    public ResponseEntity<ApiResponse<Void>> processOperation(@RequestBody CashOperationRequestDto cashOperationRequestDto) {
        log.info("CashController: process operation {}", cashOperationRequestDto);
        ApiResponse<Void> apiResponse = blockerService.isSuspiciousOperation(cashOperationRequestDto);
        if (!apiResponse.isSuccess()) {
            notificationService.sendNotification(cashOperationRequestDto.getLogin(), "Подозрительная операция. Повторите снова.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

}
