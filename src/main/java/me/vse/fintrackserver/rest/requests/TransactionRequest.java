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
public class TransactionRequest {
    private String id;
    private String accountId;
    private String forAssetId;
    private String receiverId;
    private String categoryId;
    private TransactionTypes type;
    private double amount;
    private LocalDateTime executionDateTime;
    private String note;
    private Double lat;
    private Double lon;
    private String photo;
    private Boolean isRemoved;

    private Frequencies frequency;
    private Integer remindDaysBefore;

}
