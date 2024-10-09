package me.vse.fintrackserver.rest.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAuthRequest {

    private String email;
    private String userName;
    private String password;


}
