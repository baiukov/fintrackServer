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
import me.vse.fintrackserver.rest.requests.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Transactional
    public Transaction create(TransactionRequest transactionRequest) {
        // TODO check sender
        Transaction transaction = new Transaction();
        performChecks(transactionRequest, transaction);
        entityManager.persist(transaction);
        this.addStandingOrder(transaction, transactionRequest);
        return transaction;
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
    public Transaction delete(String id) {

        // TODO check sender
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        Transaction transaction = entityManager.find(Transaction.class, id);
        if (transaction == null) {
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

    private Long checkAmount(Long amount, Long previousValue) {
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
    public void addStandingOrder(Transaction sample, TransactionRequest transactionRequest) {
        Frequencies frequency = transactionRequest.getFrequency();
        if (frequency == null) return;
        StandingOrder standingOrder = StandingOrder.builder()
                .transactionSample(sample)
                .frequency(frequency)
                .remindDaysBefore(transactionRequest.getRemindDaysBefore())
                .build();
        entityManager.persist(standingOrder);
    }

    @Transactional
    public void updateStandingOrder(TransactionRequest transactionRequest) {
        String transactionId = transactionRequest.getId();
        if (transactionId == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }
        Transaction transaction = entityManager.find(Transaction.class, transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException(ErrorMessages.TRANSACTION_DOESNT_EXIST.name());
        }

        StandingOrder standingOrder = transaction.getStandingOrder();
        if (standingOrder == null) {
            this.addStandingOrder(transaction, transactionRequest);
            return;
        }

        standingOrderMapper.updateStandingOrderFromRequest(transactionRequest, standingOrder);
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
}
