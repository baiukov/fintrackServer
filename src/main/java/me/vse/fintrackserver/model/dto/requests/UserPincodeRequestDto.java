package me.vse.fintrackserver.model.dto.requests;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPincodeRequestDto {

    private String id;
    private String pincode;

}
