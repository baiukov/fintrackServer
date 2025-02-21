package me.vse.fintrackserver.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.enums.TransactionTypes;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class StandingOrderRequest {

    private String userId;
    private String transactionId;
    private Frequencies frequency;
    private Integer remindDaysBefore;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
