package me.vse.fintrackserver.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyCodeRequesst {

    private String login;
    private String code;

}
