package ru.mityunin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String loginFrom;
    private String loginTo;
    private String accountNumberFrom;
    private String accountNumberTo;
    private BigDecimal value;
}
