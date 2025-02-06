package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.mappers.StandingOrderMapper;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.repositories.StandingOrderRepository;
import me.vse.fintrackserver.repositories.TransactionRepository;
import me.vse.fintrackserver.rest.requests.StandingOrderRequest;
import me.vse.fintrackserver.rest.requests.TransactionRequest;
import me.vse.fintrackserver.rest.responses.TransactionByCategoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StandingOrderRepository standingOrderRepository;

    @Autowired
    private StandingOrderMapper standingOrderMapper;

    @Autowired
    @Lazy
    private AccountService accountService;

    @Transactional
    public List<Transaction> findAllByAccount(String id,
                                              LocalDateTime fromDate,
                                              LocalDateTime endDate,
                                              int pageNumber
    ) {
        Account account = checkAccount(id, null);
        int batchSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, batchSize);

        if (fromDate == null && endDate != null) {
            return transactionRepository.findAllPagesByAccount(account, endDate, pageable);
        } else if (fromDate != null && endDate == null) {
            return transactionRepository.findAllPagesByAccount(account, fromDate, LocalDateTime.now(), pageable);
        } else {
            return transactionRepository.findAllPagesByAccount(account, pageable);
        }
    }

    @Transactional
    public List<TransactionByCategoryResponse> findAllByCategories(String accountId,
                                                                   LocalDateTime fromDate,
                                                                   LocalDateTime endDate,
                                                                   boolean isIncome
    ) {
        Account account = checkAccount(accountId, null);
        List<Transaction> transactionSet = isIncome ? getIncomeTransactions(account, fromDate, endDate)
                : getExpenseTransactions(account, fromDate, endDate);

        return transactionSet.stream()
                .collect(Collectors.groupingBy(transaction ->
                        Optional.ofNullable(transaction.getCategory())
                                .orElse(Category.builder().name("Other").build())))
                .entrySet().stream()
                .map(entry -> new TransactionByCategoryResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    @Transactional
    public Transaction create(TransactionRequest transactionRequest) {
        // TODO check sender
        Transaction transaction = new Transaction();
        performChecks(transactionRequest, transaction);
        entityManager.persist(transaction);
        return transaction;
    }

    @Transactional
    public StandingOrder createStandingOrder(StandingOrderRequest standingOrderRequest) throws IllegalArgumentException {
        Transaction transaction = entityManager.find(Transaction.class, standingOrderRequest.getTransactionId());
        if (transaction == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        User user = entityManager.find(User.class, standingOrderRequest.getUserId());
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        boolean doesntHaveRights = accountService.retrieveAll(user.getId()).stream()
                .map(Account::getTransactions)
                .flatMap(List::stream)
                .toList()
                .stream()
                .noneMatch(currentTransaction -> currentTransaction.getId().equals(transaction.getId()));

        if (doesntHaveRights) {
            throw new IllegalArgumentException(ErrorMessages.UNPERMITTED_OPERATION.name());
        }

        StandingOrder standingOrder = StandingOrder.builder()
                .transactionSample(transaction)
                .frequency(standingOrderRequest.getFrequency())
                .startDate(standingOrderRequest.getStartDate())
                .endDate(standingOrderRequest.getEndDate())
                .remindDaysBefore(standingOrderRequest.getRemindDaysBefore())
                .lastRepeatedAt(LocalDateTime.now())
                .build();

        entityManager.persist(standingOrder);
        return standingOrder;
    }

    @Transactional
    public StandingOrder getStandingOrder(String transactionId) throws IllegalArgumentException {
        Transaction transaction = entityManager.find(Transaction.class, transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        return transaction.getStandingOrder();
    }

    @Transactional
    public Transaction update(TransactionRequest transactionRequest) {

        // TODO check sender
        String id = transactionRequest.getId();
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        Transaction transaction = entityManager.find(Transaction.class, id);
        if (transaction == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        performChecks(transactionRequest, transaction);
        return transaction;
    }

    @Transactional
    public Transaction delete(String id, String userId) {

        // TODO check sender
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        Transaction transaction = entityManager.find(Transaction.class, id);
        if (transaction == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        if (accountService.retrieveAll(userId).stream()
                        .map(Account::getTransactions)
                        .flatMap(List::stream)
                        .toList()
                        .stream()
                        .noneMatch(currentTransaction -> currentTransaction.getId().equals(transaction.getId()))
        ) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        transactionRepository.delete(transaction);
        return transaction;
    }

    private void performChecks(TransactionRequest transactionRequest, Transaction transaction) {
        transaction.setType(checkType(transactionRequest.getType(), transaction.getType()));
        transaction.setAccount(checkAccount(transactionRequest.getAccountId(), transaction.getAccount()));
        transaction.setAmount(checkAmount(transactionRequest.getAmount(), transaction.getAmount()));
        transaction.setNote(checkNote(transactionRequest.getNote(), transaction.getNote()));
        transaction.setReceiver(checkReceiver(
                transaction.getType(), transactionRequest.getReceiverId(), transaction.getReceiver())
        );
        transaction.setForAsset(checkAsset(transactionRequest.getForAssetId(), transaction.getForAsset()));
        transaction.setCategory(checkCategory(transactionRequest.getCategoryId(), transaction.getCategory()));
        transaction.setLat(checkCoordinate(transactionRequest.getLat(), transaction.getLat()));
        transaction.setLon(checkCoordinate(transactionRequest.getLon(), transaction.getLon()));
        transaction.setExecutionDateTime(checkExecutionDateTime(
                transactionRequest.getExecutionDateTime(), transaction.getExecutionDateTime())
        );
        transaction.setPhoto(checkPhoto(transactionRequest.getPhoto(), transaction.getPhoto()));
    }

    private Account checkAccount(String id, Account previousValue) {
        if (previousValue != null) return previousValue;
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        Account account = entityManager.find(Account.class, id);
        if (account == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        return account;
    }

    private double checkAmount(Double amount, Double previousValue) {
        if (amount == null && previousValue == null) {
            throw new IllegalArgumentException(ErrorMessages.AMOUNT_LESS_THAN_0.name());
        }
        if (amount == null) return previousValue;
        if (amount <= 0) {
            throw new IllegalArgumentException(ErrorMessages.AMOUNT_LESS_THAN_0.name());
        }
        return amount;
    }

    private String checkNote(String note, String previousValue) {
        if (note != null) {
            return note.length() > 2047 ? note.substring(0, 2047) : note;
        }
        return previousValue;
    }

    private Account checkReceiver(TransactionTypes type, String receiverId, Account previousValue) {
        if (!TransactionTypes.TRANSFER.equals(type)) return null;
        if (receiverId == null && previousValue == null) {
            throw new IllegalArgumentException(ErrorMessages.RECEIVER_DOESNT_EXIST.name());
        }
        if (receiverId == null) return previousValue;
        Account receiver = entityManager.find(Account.class, receiverId);
        if (receiver == null && previousValue == null) {
            throw new IllegalArgumentException(ErrorMessages.RECEIVER_DOESNT_EXIST.name());
        }
        if (receiver == null) return previousValue;
        return receiver;
    }

    private Asset checkAsset(String assetId, Asset previousValue) {
        if (assetId != null) {
            return entityManager.find(Asset.class, assetId);
        }
        return previousValue;
    }

    private Category checkCategory(String categoryId, Category previousValue) {
        if (categoryId == null) return previousValue;
        // TODO create category
        return entityManager.find(Category.class, categoryId);
    }

    private Double checkCoordinate(Double cord, Double previousValue) {
        if (cord != null && cord >= -90 && cord <= 90) {
            return cord;
        }
        return previousValue;
    }

    private LocalDateTime checkExecutionDateTime(LocalDateTime executionDateTime, LocalDateTime previousValue) {
        if (executionDateTime != null) return executionDateTime;
        if (previousValue != null) return previousValue;
        return LocalDateTime.now();
    }

    private String checkPhoto(String photo, String previousValue) {
        if (photo != null) return photo;
        return previousValue;
    }

    private TransactionTypes checkType(TransactionTypes type, TransactionTypes previousValue) {
        if (type != null) return type;
        return previousValue;
    }

    @Transactional
    public void updateStandingOrder(StandingOrderRequest standingOrderRequest) {
        String transactionId = standingOrderRequest.getTransactionId();
        if (transactionId == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }
        Transaction transaction = entityManager.find(Transaction.class, transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        StandingOrder standingOrder = transaction.getStandingOrder();
        if (standingOrder == null) {
            this.createStandingOrder(standingOrderRequest);
            return;
        }

        standingOrderMapper.updateStandingOrderFromRequest(standingOrderRequest, standingOrder);
        standingOrderRepository.save(standingOrder);
    }

    @Transactional
    public void deleteStandingOrder(String id) {
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }
        Transaction transaction = entityManager.find(Transaction.class, id);
        StandingOrder standingOrder = entityManager.find(StandingOrder.class, id);

        if (transaction == null && standingOrder == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        standingOrder = standingOrder == null ? transaction.getStandingOrder() : standingOrder;
        standingOrderRepository.delete(standingOrder);
    }

    public List<Transaction> getExpenseTransactions(Account account, LocalDateTime fromDate, LocalDateTime endDate) {
        Predicate<Transaction> isExpense = transaction ->
                transaction.getAccount().equals(account) &&
                        (TransactionTypes.EXPENSE.equals(transaction.getType()) ||
                                TransactionTypes.COST.equals(transaction.getType()));

        Predicate<Transaction> isOutGoingTransfer = transaction ->
                transaction.getAccount().equals(account) &&
                        TransactionTypes.TRANSFER.equals(transaction.getType()) &&
                        (!transaction.getAccount().equals(transaction.getReceiver()));

        return getTransactionSet(account, fromDate, endDate)
                .stream().filter(transaction -> isExpense.test(transaction)
                        || isOutGoingTransfer.test(transaction))
                .collect(Collectors.toList());
    }

    public List<Transaction> getIncomeTransactions(Account account, LocalDateTime fromDate, LocalDateTime endDate) {
        Predicate<Transaction> isIncome = transaction ->
                transaction.getAccount().equals(account) &&
                        (TransactionTypes.INCOME.equals(transaction.getType()) ||
                                TransactionTypes.REVENUE.equals(transaction.getType()));

        Predicate<Transaction> isUpComingTransfer = transaction ->
                (!transaction.getAccount().equals(account)) &&
                        TransactionTypes.TRANSFER.equals(transaction.getType()) &&
                        account.equals(transaction.getReceiver());

        return getTransactionSet(account, fromDate, endDate)
                .stream()
                .filter(transaction -> isIncome.test(transaction)
                        || isUpComingTransfer.test(transaction))
                .collect(Collectors.toList());
    }

    public List<Transaction> getRevenueTransactions(Account account, LocalDateTime fromDate, LocalDateTime endDate) {
        Predicate<Transaction> isRevenue = transaction ->
                transaction.getAccount().equals(account) &&
                        (TransactionTypes.REVENUE.equals(transaction.getType()));

        return getTransactionSet(account, fromDate, endDate)
                .stream().filter(isRevenue).collect(Collectors.toList());
    }

    public List<Transaction> getCostTransactions(Account account, LocalDateTime fromDate, LocalDateTime endDate) {
        Predicate<Transaction> isCost = transaction ->
                transaction.getAccount().equals(account) &&
                        (TransactionTypes.COST.equals(transaction.getType()));

        return getTransactionSet(account, fromDate, endDate)
                .stream().filter(isCost).collect(Collectors.toList());
    }

    private List<Transaction> getTransactionSet(Account account, LocalDateTime fromDate, LocalDateTime endDate) {
        if (fromDate != null && endDate == null) {
            return transactionRepository.findAllByAccount(account, fromDate, LocalDateTime.now());
        } else if (fromDate == null && endDate != null) {
            return transactionRepository.findAllByAccount(account, endDate);
        } else {
            return transactionRepository.findAllByAccount(account);
        }
    }
}
