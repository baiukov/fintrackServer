package me.vse.fintrackserver.rest.requests;

import lombok.*;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.enums.TransactionTypes;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TransactionRequest {
    private String id;
    private String accountId;
    private String forAssetId;
    private String receiverId;
    private String categoryId;
    private TransactionTypes type;
    private Double amount;
    private LocalDateTime executionDateTime;
    private String note;
    private Double lat;
    private Double lon;
    private String photo;
    private Boolean isRemoved;

    private Frequencies frequency;
    private Integer remindDaysBefore;

}
