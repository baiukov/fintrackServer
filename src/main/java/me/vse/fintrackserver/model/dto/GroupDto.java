package me.vse.fintrackserver.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GroupDto {

    private String id;
    private String name;
    private String adminId;
    private List<String> memberIds;
    private List<String> accountIds;

}
