package me.vse.fintrackserver.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAuthGoogleRequest {

    private String token;
    private String platform;
}
