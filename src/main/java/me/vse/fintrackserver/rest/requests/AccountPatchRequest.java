package me.vse.fintrackserver.rest.requests;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vse.fintrackserver.model.Account;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountPatchRequest {

    private Account account;

}
