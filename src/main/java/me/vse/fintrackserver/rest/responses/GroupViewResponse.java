package me.vse.fintrackserver.rest.responses;

import lombok.*;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.SimplifiedEntityDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class GroupViewResponse {

    private String id;
    private String ownerId;
    private String name;
    private List<User> users;
    private List<Account> accounts;

}
