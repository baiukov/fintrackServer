package me.vse.fintrackserver.model.dto.responses;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAuthResponseDto {

    private String id;
    private String email;
    private String userName;
    private boolean isBlocked;
    private boolean isAdmin;

}
