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
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.service.CashService;

@Controller
@RequestMapping("/cash")
public class CashController {
    private static final Logger log = LoggerFactory.getLogger(CashController.class);
    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/action")
    public String actionWithPaymentAccount(
            @Valid @ModelAttribute("cashOperationRequestDto") CashOperationRequestDto cashOperationRequestDto,
            RedirectAttributes redirectAttributes) {
        ApiResponse<Void> apiResponse = cashService.processOperation(cashOperationRequestDto);
        log.info("CashController: api response {}", apiResponse);
        if (!apiResponse.isSuccess()) {
            log.info("CashController: api message {}", apiResponse.getMessage());
            redirectAttributes.addFlashAttribute("actionWithPaymentAccountError", apiResponse.getMessage());
        }
        return "redirect:/home";
    }
}
