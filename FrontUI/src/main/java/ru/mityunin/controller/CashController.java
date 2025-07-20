package ru.mityunin.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.service.CashService;

@Controller
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/action")
    public String actionWithPaymentAccount(@Valid @ModelAttribute("cashOperationRequest") CashOperationRequest cashOperationRequest) {
        cashService.processOperation(cashOperationRequest);
        return "redirect:/home";
    }
}
