package ru.mityunin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.service.CashService;

@Controller
@RequestMapping("/cash")
public class CashController {
    private CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }



    @PostMapping("/processOperation")
    public ResponseEntity<Boolean> processOperation(@RequestBody CashOperationRequest cashOperationRequest) {

        boolean isSuccess = cashService.processOperation(cashOperationRequest);
        if (isSuccess) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }
}
