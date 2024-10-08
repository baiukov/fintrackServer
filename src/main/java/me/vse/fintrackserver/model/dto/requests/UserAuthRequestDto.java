package me.vse.fintrackserver.model.dto.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAuthRequestDto {

    private String email;
    private String userName;
    private String password;


}
