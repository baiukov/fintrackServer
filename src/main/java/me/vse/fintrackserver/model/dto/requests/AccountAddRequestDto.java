package me.vse.fintrackserver.model.dto.requests;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vse.fintrackserver.enums.AccountType;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountAddRequestDto {

    private String ownerId;
    private String name;
    private String type;
    private String currency;
    private Long initialAmount = 0L;
    private double interestRate = 1;
    private Long goalAmount = 0L;
    private Long alreadyPaidAmount = 0L;
    private boolean isRemoved = false;

}
