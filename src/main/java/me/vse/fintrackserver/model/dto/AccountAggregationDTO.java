package me.vse.fintrackserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountAggregationDTO {
    private String accountId;
    private Double totalIncome;
    private Double totalExpense;
}

