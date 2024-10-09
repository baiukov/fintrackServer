package me.vse.fintrackserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vse.fintrackserver.enums.AccountType;

import java.util.Currency;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountDto {
    private String id;
    private String name;
    private AccountType type;
    private Currency currency;
    private Long initialAmount;
    private double interestRate;
    private Long goalAmount;
    private Long alreadyPaidAmount;
    private boolean isRemoved;

}
