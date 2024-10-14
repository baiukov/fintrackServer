package me.vse.fintrackserver.services;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.enums.UserRights;
import me.vse.fintrackserver.mappers.AccountMapper;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.Transaction;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.repositories.TransactionRepository;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
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

@Service
public class AccountService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountMapper accountMapper;


    @Transactional
    public Long getBalance(String id, LocalDateTime fromDate, LocalDateTime endDate) {
        Account account = checkAccount(id);

        AtomicReference<Long> initialAmount = new AtomicReference<>(account.getInitialAmount());

        Predicate<Transaction> isIncome = transaction ->
            transaction.getAccount().equals(account) &&
                    (TransactionTypes.INCOME.equals(transaction.getType()) ||
                    TransactionTypes.REVENUE.equals(transaction.getType()));

        Predicate<Transaction> isExpense = transaction ->
            transaction.getAccount().equals(account) &&
                    (TransactionTypes.EXPENSE.equals(transaction.getType()) ||
                    TransactionTypes.COST.equals(transaction.getType()));

        Predicate<Transaction> isOutGoingTransfer = transaction ->
                transaction.getAccount().equals(account) &&
                TransactionTypes.TRANSFER.equals(transaction.getType()) &&
                (!transaction.getAccount().equals(transaction.getReceiver()));

        Predicate<Transaction> isUpComingTransfer = transaction ->
                (!transaction.getAccount().equals(account)) &&
                TransactionTypes.TRANSFER.equals(transaction.getType()) &&
                account.equals(transaction.getReceiver());

        Consumer<Transaction> increaseConsumer = transaction -> {
            if (isIncome.test(transaction) || isUpComingTransfer.test(transaction)) {
                initialAmount.updateAndGet(v -> v + transaction.getAmount());
            }
        };

        Consumer<Transaction> decreaseConsumer = transaction -> {
            if (isExpense.test(transaction) || isOutGoingTransfer.test(transaction)) {
                initialAmount.updateAndGet(v -> v - transaction.getAmount());
            }
        };

        if (fromDate != null && endDate == null) {
            transactionRepository.findAllByAccount(account, fromDate, LocalDateTime.now())
                .stream()
                .filter(Objects::nonNull)
                .forEach(increaseConsumer.andThen(decreaseConsumer));
        } else if (fromDate == null && endDate != null) {
            transactionRepository.findAllByAccount(account, endDate)
                .stream()
                .filter(Objects::nonNull)
                .forEach(increaseConsumer.andThen(decreaseConsumer));
        } else {
            transactionRepository.findAllByAccount(account)
                .stream()
                .filter(Objects::nonNull)
                .forEach(increaseConsumer.andThen(decreaseConsumer));
        }

        return initialAmount.get();
    }

    @Transactional
    public Long getIncomes(String id) {
        Account account = checkAccount(id);

        AtomicReference<Long> initialAmount = new AtomicReference<>(account.getInitialAmount());

        Predicate<Transaction> isIncome = transaction ->
            transaction.getAccount().equals(account) &&
                    (TransactionTypes.INCOME.equals(transaction.getType()) ||
                    TransactionTypes.REVENUE.equals(transaction.getType()));

        Predicate<Transaction> isExpense = transaction ->
            transaction.getAccount().equals(account) &&
                    (TransactionTypes.EXPENSE.equals(transaction.getType()) ||
                    TransactionTypes.COST.equals(transaction.getType()));

        Predicate<Transaction> isOutGoingTransfer = transaction ->
                transaction.getAccount().equals(account) &&
                TransactionTypes.TRANSFER.equals(transaction.getType()) &&
                (!transaction.getAccount().equals(transaction.getReceiver()));

        Predicate<Transaction> isUpComingTransfer = transaction ->
                (!transaction.getAccount().equals(account)) &&
                TransactionTypes.TRANSFER.equals(transaction.getType()) &&
                account.equals(transaction.getReceiver());

        Consumer<Transaction> increaseConsumer = transaction -> {
            if (isIncome.test(transaction) || isUpComingTransfer.test(transaction)) {
                initialAmount.updateAndGet(v -> v + transaction.getAmount());
            }
        };

        Consumer<Transaction> decreaseConsumer = transaction -> {
            if (isExpense.test(transaction) || isOutGoingTransfer.test(transaction)) {
                initialAmount.updateAndGet(v -> v - transaction.getAmount());
            }
        };

        transactionRepository.findAllByAccount(account)
                .stream()
                .filter(Objects::nonNull)
                .forEach(increaseConsumer.andThen(decreaseConsumer));

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
