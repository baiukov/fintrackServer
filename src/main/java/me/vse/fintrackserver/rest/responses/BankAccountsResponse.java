package me.vse.fintrackserver.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.vse.fintrackserver.model.bankIdentity.Account;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BankAccountsResponse {

    private boolean areAccountsLoaded;

    private String exception;

    private List<Account> accounts;

}
