package me.vse.fintrackserver.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupDto {

    private String id;
    private String name;
    private String adminId;
    private List<String> memberIds;

}
