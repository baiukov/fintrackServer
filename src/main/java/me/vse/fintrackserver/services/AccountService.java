package me.vse.fintrackserver.services;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.UserRights;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.requests.AccountAddRequestDto;
import me.vse.fintrackserver.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AccountService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public Account add(AccountAddRequestDto accountRequest) {

        User owner = entityManager.find(User.class, accountRequest.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        if (StringUtils.isBlank(accountRequest.getName())) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_NAME_IS_BLANK.name());
        }

        AccountType type = AccountType.valueOf(accountRequest.getType());

        Currency currency = Currency.getInstance(accountRequest.getCurrency());
        if (currency == null) {
            throw new IllegalArgumentException(ErrorMessages.WRONG_CURRENCY.name());
        }

        Account account = Account.builder()
                .name(accountRequest.getName())
                .type(type)
                .currency(currency)
                .initialAmount(accountRequest.getInitialAmount())
                .interestRate(accountRequest.getInterestRate())
                .alreadyPaidAmount(accountRequest.getAlreadyPaidAmount())
                .build();

        entityManager.persist(account);

        AccountUserRights userRights = AccountUserRights.builder()
                .user(owner)
                .account(account)
                .rights(UserRights.WRITE)
                .isOwner(true)
                .build();

        entityManager.persist(userRights);

        return account;
    }

    public List<Account> retrieveAll(String userId) {
        return entityManager.find(User.class, userId).getAccountUserRights()
                .stream()
                .map(AccountUserRights::getAccount)
                .collect(Collectors.toList());
    }

}
