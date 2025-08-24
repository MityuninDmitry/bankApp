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
import ru.mityunin.dto.TransferRequestDto;
import ru.mityunin.service.BlockerService;
import ru.mityunin.service.NotificationService;
import ru.mityunin.service.TransferService;

@Controller
@RequestMapping("/api")
public class TransferController {
    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;
    private final NotificationService notificationService;
    private final BlockerService blockerService;
    public TransferController(TransferService transferService, NotificationService notificationService, BlockerService blockerService) {
        this.transferService = transferService;
        this.notificationService = notificationService;
        this.blockerService = blockerService;
    }

    @PostMapping("/transferRequest")
    public ResponseEntity<ApiResponse<Void>> transferRequest(@RequestBody TransferRequestDto transferRequestDto) {
        log.info("[Transfer] TransferController: transferRequestDto {}", transferRequestDto);
        ApiResponse<Void> isBlockerResponse = blockerService.isBlockerOperation();
        if (isBlockerResponse.isSuccess()) {
            ApiResponse<Void> apiResponse = transferService.transferOperation(transferRequestDto);
            if (apiResponse.isSuccess()) {
                notificationService.sendNotification(transferRequestDto.getLogin(), apiResponse.getMessage());
                return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
            } else {
                notificationService.sendNotification(transferRequestDto.getLogin(), apiResponse.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }
        } else {
            notificationService.sendNotification(transferRequestDto.getLogin(), isBlockerResponse.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(isBlockerResponse);
        }

    }
}
