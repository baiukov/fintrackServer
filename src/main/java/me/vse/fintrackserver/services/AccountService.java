package me.vse.fintrackserver.services;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import me.vse.fintrackserver.controller.AssetController;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.enums.UserRights;
import me.vse.fintrackserver.mappers.AccountMapper;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.repositories.TransactionRepository;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@AllArgsConstructor
@Builder
public class AccountService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AssetService assetService;

    @Autowired
    private TransactionService transactionService;

    @Transactional
    public Double getNetWorth(String id, LocalDateTime fromDate, LocalDateTime endDate) {
        Account account = checkAccount(id);

        AtomicReference<Double> initialAmount = new AtomicReference<>(account.getInitialAmount());

        Consumer<Double> increaseConsumer = amount -> {
            initialAmount.updateAndGet(v -> v + amount);
        };

        account.getAssets().stream()
                .filter(Objects::nonNull)
                .filter(not(Asset::isRemoved))
                .map(assetService::getCurrentAssetPrice)
                .forEach(increaseConsumer);

        if (AccountType.BUSINESS_ACCOUNT.equals(account.getType())) {
            transactionService.getRevenueTransactions(account, fromDate, endDate)
                    .forEach(transaction -> initialAmount.updateAndGet(v -> v + transaction.getAmount()));
            transactionService.getCostTransactions(account, fromDate, endDate)
                    .forEach(transaction -> initialAmount.updateAndGet(v -> v - transaction.getAmount()));
        } else {
            transactionService.getIncomeTransactions(account, fromDate, endDate)
                    .forEach(transaction -> initialAmount.updateAndGet(v -> v + transaction.getAmount()));
            transactionService.getExpenseTransactions(account, fromDate, endDate)
                    .forEach(transaction -> initialAmount.updateAndGet(v -> v - transaction.getAmount()));
        }
        return initialAmount.get();
    }

    @Transactional
    public Double getBalance(String id, LocalDateTime fromDate, LocalDateTime endDate) {
        return getIncome(id, fromDate, endDate) + getExpense(id, fromDate, endDate);
    }

    @Transactional
    public Double getIncome(String id, LocalDateTime fromDate, LocalDateTime endDate) {
        Account account = checkAccount(id);

        AtomicReference<Double> initialAmount = new AtomicReference<>(account.getInitialAmount());

        Consumer<Transaction> increaseConsumer = transaction -> {
            initialAmount.updateAndGet(v -> v + transaction.getAmount());
        };

        transactionService.getIncomeTransactions(account, fromDate, endDate).forEach(increaseConsumer);
        return initialAmount.get();
    }

    @Transactional
    public Double getExpense(String id, LocalDateTime fromDate, LocalDateTime endDate) {
        Account account = checkAccount(id);

        AtomicReference<Double> initialAmount = new AtomicReference<>(account.getInitialAmount());

        Consumer<Transaction> decreaseConsumer = transaction -> {
                initialAmount.updateAndGet(v -> v - transaction.getAmount());
        };

        transactionService.getExpenseTransactions(account, fromDate, endDate).forEach(decreaseConsumer);

        return initialAmount.get();
    }

    @Transactional
    public Account add(AccountAddRequest accountRequest) {

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

    @Transactional
    public List<Account> retrieveAll(String userId) {
        return entityManager.find(User.class, userId).getAccountUserRights()
                .stream()
                .filter(Objects::nonNull)
                .map(AccountUserRights::getAccount)
                .collect(Collectors.toList());
    }

    @Transactional
    public Account update(AccountDto accountDto) {
        Account record = entityManager.find(Account.class, accountDto.getId());
        accountMapper.updateAccountFromDto(accountDto, record);
        accountRepository.save(record);
        return record;
    }

    private Account checkAccount(String id) {
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        Account account = entityManager.find(Account.class, id);
        if (account == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        return account;
    }

}
