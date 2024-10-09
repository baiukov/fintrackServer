package me.vse.fintrackserver.rest.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPincodeRequest {

    private String id;
    private String pincode;

}
