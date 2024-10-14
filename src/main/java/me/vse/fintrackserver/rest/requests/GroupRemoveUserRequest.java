package me.vse.fintrackserver.rest.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupRemoveUserRequest {

    private String groupId;
    private String userId;
}
