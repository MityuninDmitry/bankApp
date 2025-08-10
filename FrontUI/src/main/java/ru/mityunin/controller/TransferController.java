package ru.mityunin.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.dto.TransferRequest;
import ru.mityunin.service.CashService;
import ru.mityunin.service.TransferService;

@Controller
@RequestMapping("/transfer")
public class TransferController {
    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transferRequest")
    public String transferRequest(
            @ModelAttribute("transferRequest") TransferRequest transferRequest,
            RedirectAttributes redirectAttributes) {
        ApiResponse<Void> apiResponse = transferService.transferRequest(transferRequest);
        log.info("[FrontUI] TransferController: api response {}", apiResponse);
        if (!apiResponse.isSuccess()) {
            log.info("[FrontUI] TransferController: api message {}", apiResponse.getMessage());
            redirectAttributes.addFlashAttribute("transferError", apiResponse.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/transferToUser")
    public String transferToUser(
            @ModelAttribute("transferRequest") TransferRequest transferRequest,
            RedirectAttributes redirectAttributes) {
        ApiResponse<Void> apiResponse = transferService.transferRequest(transferRequest);
        log.info("[FrontUI] TransferController: transfer to user api response {}", apiResponse);
        if (!apiResponse.isSuccess()) {
            log.info("[FrontUI] TransferController: transfer to user api message {}", apiResponse.getMessage());
            redirectAttributes.addFlashAttribute("transferToUserError", apiResponse.getMessage());
        }
        return "redirect:/home";
    }
}
