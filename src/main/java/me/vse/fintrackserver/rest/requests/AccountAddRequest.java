package me.vse.fintrackserver.rest.requests;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountAddRequest {

    private String ownerId;
    private String name;
    private String type;
    private String currency;
    private double initialAmount;
    private double interestRate = 1;
    private Long goalAmount = 0L;
    private Long alreadyPaidAmount = 0L;
    private boolean isRemoved = false;

}
