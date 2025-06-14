package me.vse.fintrackserver.rest.responses;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAuthResponse {

    private String id;
    private String email;
    private String userName;
    private boolean hasPincode;
    private boolean isBlocked;
    private boolean isAdmin;
    private String accessToken;
    private String refreshToken;
}
