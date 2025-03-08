package me.vse.fintrackserver.model.dto;

import lombok.*;
import me.vse.fintrackserver.enums.AccountType;

import java.util.Currency;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountDto {
    private String id;
    private String name;
    private AccountType type;
    private String emoji;
    private Currency currency;
    private Double initialAmount;
    private Double interestRate;
    private Long goalAmount;
    private Long alreadyPaidAmount;
    private Boolean isRemoved;

}
