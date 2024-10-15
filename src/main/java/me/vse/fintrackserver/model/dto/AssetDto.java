package me.vse.fintrackserver.model.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AssetDto {

    private String senderId;
    private String id;
    private String name;
    private String type;
    private String accountId;
    private double acquisitionPrice;
    private double depreciationPrice;
    private LocalDate startDateStr = LocalDate.now();
    private LocalDate endDateStr;
    private String color;
    private String icon;
    private boolean isRemoved;

}
