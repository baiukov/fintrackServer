package me.vse.fintrackserver.model.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class CategoryDto {

    private String id;
    private String icon;
    private String name;
    private String color;

}
