package me.vse.fintrackserver.model.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AssetDto {

    private String senderId;
    private String id;
    private String name;
    private String type;
    private String accountId;
    private Double acquisitionPrice;
    private Double depreciationPrice;
    private LocalDate startDateStr = LocalDate.now();
    private LocalDate endDateStr;
    private String color;
    private String icon;
    private boolean isRemoved;

}
