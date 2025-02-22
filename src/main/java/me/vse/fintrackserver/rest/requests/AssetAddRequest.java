package me.vse.fintrackserver.rest.requests;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AssetAddRequest {

    private String senderId;
    private String name;
    private String type;
    private String accountId;
    private double acquisitionPrice;
    private double depreciationPrice;
    private LocalDate startDateStr = LocalDate.now();
    private LocalDate endDateStr;
    private String icon;

}
