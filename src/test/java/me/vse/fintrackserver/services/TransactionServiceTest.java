package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.mappers.StandingOrderMapper;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.repositories.StandingOrderRepository;
import me.vse.fintrackserver.repositories.TransactionRepository;
import me.vse.fintrackserver.rest.requests.TransactionRequest;
import org.apache.logging.log4j.util.Strings;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.vse.fintrackserver.ATest.randomString;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionServiceTest {

    private EntityManager entityManager;
    private TransactionRepository transactionRepository;
    private StandingOrderRepository standingOrderRepository;
    private StandingOrderMapper standingOrderMapper;
    private TransactionService transactionService;

    @BeforeEach
    public void setUp() {
        entityManager = EasyMock.mock(EntityManager.class);
        transactionRepository = EasyMock.mock(TransactionRepository.class);
        standingOrderRepository = EasyMock.mock(StandingOrderRepository.class);
        standingOrderMapper = EasyMock.mock(StandingOrderMapper.class);
        transactionService = new TransactionService(entityManager, transactionRepository,
                standingOrderRepository, standingOrderMapper);
    }

    private Stream<Arguments> getCreateTransactionScenarios() {
        return Stream.of(
            Arguments.of(TransactionRequest.builder().build(), null, ErrorMessages.ACCOUNT_DOESNT_EXIST),
            Arguments.of(TransactionRequest.builder().accountId("").build(), null, ErrorMessages.ACCOUNT_DOESNT_EXIST),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .amount(-100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    ErrorMessages.AMOUNT_LESS_THAN_0),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .executionDateTime(LocalDateTime.now())
                            .amount(100.0)
                            .build(),
                    ErrorMessages.RECEIVER_DOESNT_EXIST),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .executionDateTime(LocalDateTime.now())
                            .receiver(Account.builder().build())
                            .amount(100.0)
                            .build(),
                    ErrorMessages.RECEIVER_DOESNT_EXIST),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.INCOME)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.INCOME)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.EXPENSE)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.EXPENSE)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.REVENUE)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.REVENUE)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.COST)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.COST)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId2")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId2").build())
                            .type(TransactionTypes.TRANSFER)
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("Some note for the transaction")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("Some note for the transaction")
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note(randomString(2050))
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("$strip2047")
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(100.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-100.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(40.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .lon(100.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .lon(-100.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .lon(40.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .lon(40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .lon(-40.0)
                            .amount(100.0)
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .lon(-40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.now())
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .lon(-40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .lon(-40.0)
                            .amount(100.0)
                            .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                            .build(),
                    null),
            Arguments.of(TransactionRequest.builder()
                            .accountId("accId")
                            .type(TransactionTypes.TRANSFER)
                            .receiverId("anotherAccId")
                            .note("anyNote")
                            .forAssetId("assetId")
                            .categoryId("categoryId")
                            .lat(-40.0)
                            .lon(-40.0)
                            .amount(100.0)
                            .photo("my_groceries.png")
                            .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                            .build(),
                    Transaction.builder()
                            .account(Account.builder().id("accId").build())
                            .type(TransactionTypes.TRANSFER)
                            .note("anyNote")
                            .forAsset(Asset.builder().id("assetId").build())
                            .category(Category.builder().id("categoryId").build())
                            .receiver(Account.builder().id("anotherAccId").build())
                            .lat(-40.0)
                            .lon(-40.0)
                            .amount(100.0)
                            .photo("my_groceries.png")
                            .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                            .build(),
                    null)
        );
    }

    @ParameterizedTest(name = "Test create transaction. Given transaction request {0}. Should return transaction {1} or throw " +
            "exception {2}")
    @MethodSource("getCreateTransactionScenarios")
    public void createTest(TransactionRequest request, Transaction transaction, ErrorMessages message) {
        expect(entityManager.find(Account.class, request.getAccountId()))
                .andReturn(Strings.isBlank(request.getAccountId()) ? null : transaction.getAccount());
        if (transaction != null && request.getReceiverId() != null) {
            expect(entityManager.find(Account.class, request.getReceiverId()))
                    .andReturn(Strings.isBlank(request.getReceiverId()) ? null : transaction.getReceiver());
        }
        if (transaction != null && request.getForAssetId() != null) {
            expect(entityManager.find(Asset.class, request.getForAssetId()))
                    .andReturn(Strings.isBlank(request.getForAssetId()) ? null : transaction.getForAsset());
        }
        if (transaction != null && request.getCategoryId() != null) {
            expect(entityManager.find(Category.class, request.getCategoryId()))
                    .andReturn(Strings.isBlank(request.getCategoryId()) ? null : transaction.getCategory());
        }
        entityManager.persist(transaction);
        replay(entityManager);

        if (transaction != null && "$strip2047".equals(transaction.getNote())) {
            transaction.setNote(request.getNote().substring(0, 2047));
        }

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.create(request));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            assertEquals(transaction, transactionService.create(request));
            verify(entityManager);
        }
    }

    private Stream<Arguments> getUpdateScenarios() {
        return Stream.of(
                Arguments.of(new TransactionRequest(), null, ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of(TransactionRequest.builder().id("").build(), null, ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .build(),
                        new Transaction(),
                        ErrorMessages.ACCOUNT_DOESNT_EXIST
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("")
                                .build(),
                        new Transaction(),
                        ErrorMessages.ACCOUNT_DOESNT_EXIST
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .account(Account.builder().id("accId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .amount(100.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .account(Account.builder().id("newAccId").build())
                                .amount(100.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .account(Account.builder().id("newAccId").build())
                                .amount(100.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .amount(-100.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .account(Account.builder().id("newAccId").build())
                                .amount(100.0)
                                .build(),
                        ErrorMessages.AMOUNT_LESS_THAN_0
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .amount(100.0)
                                .note(randomString(2050))
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .account(Account.builder().id("newAccId").build())
                                .amount(100.0)
                                // note will be added during the test [0, 2047)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.TRANSFER)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        ErrorMessages.RECEIVER_DOESNT_EXIST
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.TRANSFER)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        ErrorMessages.RECEIVER_DOESNT_EXIST
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .receiverId("")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.TRANSFER)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        ErrorMessages.RECEIVER_DOESNT_EXIST
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.TRANSFER)
                                .receiverId("")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        ErrorMessages.RECEIVER_DOESNT_EXIST
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.TRANSFER)
                                .receiverId("")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.TRANSFER)
                                .receiver(Account.builder().id("receiverId").build())
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.TRANSFER)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.TRANSFER)
                                .receiver(Account.builder().id("receiverId").build())
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.TRANSFER)
                                .receiverId("newReceiverId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.TRANSFER)
                                .receiver(Account.builder().id("newReceiverId").build())
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.COST)
                                .forAssetId("")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.COST)
                                .forAssetId("newAssetId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .forAsset(Asset.builder().id("newAssetId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.COST)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .forAsset(Asset.builder().id("newAssetId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.COST)
                                .categoryId("")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.COST)
                                .categoryId("newCategoryId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .category(Category.builder().id("newCategoryId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.COST)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .category(Category.builder().id("newCategoryId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lat(180.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lat(-180.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lat(45.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .lat(45.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lat(-45.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .lat(-45.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .lat(-45.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lon(180.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lon(-180.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lon(-45.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .lon(-45.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .lon(45.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .lon(45.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .lon(45.0)
                                .lat(45.0)
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .executionDateTime(LocalDateTime.of(2024, 3, 8, 12, 45))
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .executionDateTime(LocalDateTime.now())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .photo("groceries.png")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .photo("groceries.png")
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.COST)
                                .account(Account.builder().id("newAccId").build())
                                .photo("groceries.png")
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .type(TransactionTypes.INCOME)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.INCOME)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .type(TransactionTypes.INCOME)
                                .account(Account.builder().id("newAccId").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .amount(250.0)
                                .type(TransactionTypes.INCOME)
                                .photo("groceries.jpg")
                                .account(Account.builder().id("newAccId").build())
                                .note(randomString(50))
                                .forAsset(Asset.builder().id("assetId").name("car").build())
                                .lat(50.0)
                                .lon(40.0)
                                .executionDateTime(LocalDateTime.of(2024, 4, 5, 16, 48))
                                .category(Category.builder().id("catId").name("food").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .accountId("newAccId")
                                .amount(300.0)
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .amount(300.0)
                                .type(TransactionTypes.INCOME)
                                .photo("groceries.jpg")
                                .account(Account.builder().id("newAccId").build())
                                .note(randomString(50))
                                .forAsset(Asset.builder().id("assetId").name("car").build())
                                .lat(50.0)
                                .lon(40.0)
                                .executionDateTime(LocalDateTime.of(2024, 4, 5, 16, 48))
                                .category(Category.builder().id("catId").name("food").build())
                                .build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .id("transacId")
                                .amount(300.0)
                                .type(TransactionTypes.INCOME)
                                .photo("groceries.jpg")
                                .accountId("newAccId")
                                .note("randomNote")
                                .forAssetId("assetId")
                                .lat(50.0)
                                .lon(40.0)
                                .executionDateTime(LocalDateTime.of(2024, 4, 5, 16, 48))
                                .categoryId("catId")
                                .build(),
                        Transaction.builder()
                                .id("transacId")
                                .amount(300.0)
                                .type(TransactionTypes.INCOME)
                                .photo("groceries.jpg")
                                .account(Account.builder().id("newAccId").build())
                                .note(randomString(50))
                                .forAsset(Asset.builder().id("assetId").build())
                                .lat(50.0)
                                .lon(40.0)
                                .executionDateTime(LocalDateTime.of(2024, 4, 5, 16, 48))
                                .category(Category.builder().id("catId").build())
                                .build(),
                        null
                )
        );
    }

    @ParameterizedTest(name = "Test update transaction. Given transaction request {0}. Should return transaction {1} or throw " +
            "exception {2}")
    @MethodSource("getUpdateScenarios")
    public void updateTest(TransactionRequest request, Transaction transaction, ErrorMessages message) {
        expect(entityManager.find(Transaction.class, request.getId()))
                .andReturn(Strings.isBlank(request.getId()) ? null : transaction);
        if (transaction != null && request.getAccountId() != null) {
            expect(entityManager.find(Account.class, request.getAccountId()))
                    .andReturn(Strings.isBlank(request.getAccountId()) ? null : transaction.getAccount());
        }

        if (transaction != null && request.getReceiverId() != null) {
            expect(entityManager.find(Account.class, request.getReceiverId()))
                    .andReturn(Strings.isBlank(request.getReceiverId()) ? null : transaction.getReceiver());
        }
        if (transaction != null && request.getForAssetId() != null) {
            expect(entityManager.find(Asset.class, request.getForAssetId()))
                    .andReturn(Strings.isBlank(request.getForAssetId()) ? null : transaction.getForAsset());
        }
        if (transaction != null && request.getCategoryId() != null) {
            expect(entityManager.find(Category.class, request.getCategoryId()))
                    .andReturn(Strings.isBlank(request.getCategoryId()) ? null : transaction.getCategory());
        }
        expect(transactionRepository.save(transaction)).andReturn(transaction);
        replay(entityManager, transactionRepository);

        if (transaction != null && "$strip2047".equals(transaction.getNote())) {
            transaction.setNote(request.getNote().substring(0, 2047));
        }

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.update(request));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            assertEquals(transaction, transactionService.update(request));
            verify(entityManager);
        }
    }

    private Stream<Arguments> getDeleteScenarios() {
        return Stream.of(
                Arguments.of(null, ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of("", ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of("transactionId", null)
        );
    }

    @ParameterizedTest(name = "Test delete transactions. Give id: {0}. Should thrown an exeption {1} or " +
            "remove transaction")
    @MethodSource("getDeleteScenarios")
    public void deleteTest(String id, ErrorMessages exception) {
        Transaction transaction = new Transaction();
        expect(entityManager.find(Transaction.class, id))
                .andReturn(Strings.isBlank(id) ? null : transaction);
        transactionRepository.delete(transaction);
        replay(entityManager, transactionRepository);

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.delete(id));
            assertEquals(exception.name(), thrown.getMessage());
        } else {
            transactionService.delete(id);
            verify(entityManager, transactionRepository);
        }
    }

    private Stream<Arguments> getAddStandingOrderScenarios() {
        return Stream.of(
                Arguments.of(TransactionRequest.builder().build(), null),
                Arguments.of(
                        TransactionRequest.builder().frequency(Frequencies.DAILY).build(),
                        null
                ),
                Arguments.of(
                        TransactionRequest.builder().frequency(Frequencies.DAILY).build(),
                        StandingOrder.builder()
                                .transactionSample(Transaction.builder().id("transacId").build())
                                .frequency(Frequencies.DAILY)
                                .build()
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .frequency(Frequencies.MONTHLY)
                                .remindDaysBefore(5)
                                .build(),
                        StandingOrder.builder()
                                .transactionSample(Transaction.builder().id("transacId").build())
                                .frequency(Frequencies.MONTHLY)
                                .remindDaysBefore(5)
                                .build()
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .frequency(Frequencies.WEEKLY)
                                .remindDaysBefore(1)
                                .build(),
                        StandingOrder.builder()
                                .transactionSample(Transaction.builder().id("transacId").build())
                                .frequency(Frequencies.WEEKLY)
                                .remindDaysBefore(1)
                                .build()
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .frequency(Frequencies.QUARTERLY)
                                .remindDaysBefore(30)
                                .build(),
                        StandingOrder.builder()
                                .transactionSample(Transaction.builder().id("transacId").build())
                                .frequency(Frequencies.QUARTERLY)
                                .remindDaysBefore(30)
                                .build()
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .frequency(Frequencies.YEARLY)
                                .remindDaysBefore(30)
                                .build(),
                        StandingOrder.builder()
                                .transactionSample(Transaction.builder().id("transacId").build())
                                .frequency(Frequencies.YEARLY)
                                .remindDaysBefore(30)
                                .build()
                )
        );
    }

    @ParameterizedTest(name = "Test add standing order. Given request: {0}. Should return standing order: {1}")
    @MethodSource("getAddStandingOrderScenarios")
    public void addStandingOrderTest(TransactionRequest request, StandingOrder expected) {
        if (expected == null) {
            replay(entityManager);
            transactionService.addStandingOrder(null, request);
            verify(entityManager);
        } else {
            entityManager.persist(expected);
            replay(entityManager);
            transactionService.addStandingOrder(expected.getTransactionSample(), request);
            verify(entityManager);
        }

    }

    private Stream<Arguments> updateStandingOrderScenarios() {
        return Stream.of(
                Arguments.of(new TransactionRequest(),
                        new Transaction(),
                        ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of(TransactionRequest.builder().id("").build(),
                        new Transaction(),
                        ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of(TransactionRequest.builder()
                                .id("transactionId")
                                .frequency(Frequencies.DAILY)
                                .build(),
                        Transaction.builder()
                                .id("transactionId")
                                .standingOrder(StandingOrder.builder()
                                        .frequency(Frequencies.DAILY)
                                        .transactionSample(Transaction.builder().id("transactionId").build())
                                        .build())
                                .build(),
                        null),
                Arguments.of(TransactionRequest.builder()
                                .id("transactionId")
                                .frequency(Frequencies.DAILY)
                                .remindDaysBefore(5)
                                .build(),
                        Transaction.builder()
                                .id("transactionId")
                                .standingOrder(StandingOrder.builder()
                                        .frequency(Frequencies.DAILY)
                                        .transactionSample(Transaction.builder().id("transactionId").build())
                                        .remindDaysBefore(5)
                                        .build())
                                .build(),
                        null)
        );
    }

    @ParameterizedTest(name = "Test update standing order. Given request: {0}, transaction expected: {1}. " +
            "Should throw exception {2} or complete update")
    @MethodSource("updateStandingOrderScenarios")
    public void updateStandingOrderTest(TransactionRequest request, Transaction expected, ErrorMessages exception) {
        standingOrderMapper.updateStandingOrderFromRequest(request, expected.getStandingOrder());
        expect(standingOrderRepository.save(expected.getStandingOrder())).andReturn(expected.getStandingOrder());
        expect(entityManager.find(Transaction.class, request.getId()))
                .andReturn(Strings.isBlank(expected.getId()) ? null : expected);
        replay(standingOrderMapper, standingOrderRepository, entityManager);

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.updateStandingOrder(request));
            assertEquals(exception.name(), thrown.getMessage());
        } else {
            transactionService.updateStandingOrder(request);
            verify(standingOrderRepository, standingOrderMapper, entityManager);
        }


    }

    private Stream<Arguments> deleteStandingOrderScenarios() {
        return Stream.of(
                Arguments.of(null, new Transaction(), new StandingOrder(), ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of("", new Transaction(), new StandingOrder(), ErrorMessages.TRANSACTION_DOESNT_EXIST),
                Arguments.of("standingOrderId",
                        new Transaction(),
                        StandingOrder.builder().id("standingOrderId").build(),
                        null),
                Arguments.of("standingOrderId",
                        Transaction.builder()
                                .standingOrder(StandingOrder.builder().id("standingOrderId").build())
                                .build(),
                        new StandingOrder(),
                        null)
        );
    }

    @ParameterizedTest(name = "Test delete standing order id. Given id: {0}, related transaction: {1}, " +
            "standing order to be deleted: {2}. Should thrown exception {3} or delete standing order")
    @MethodSource("deleteStandingOrderScenarios")
    public void deleteStandingOrderTest(String id, Transaction transaction, StandingOrder standingOrder,
                                        ErrorMessages exception) {
        expect(entityManager.find(Transaction.class, id))
                .andReturn(Strings.isBlank(id) ? null : transaction);
        expect(entityManager.find(StandingOrder.class, id))
                .andReturn(Strings.isBlank(id) ? null : standingOrder);
        standingOrderRepository.delete(standingOrder);
        replay(entityManager, standingOrderRepository);

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.deleteStandingOrder(id));
            assertEquals(exception.name(), thrown.getMessage());
        } else {
            transactionService.deleteStandingOrder(id);
            verify(entityManager, standingOrderRepository);
        }
    }

    private Stream<Arguments> getExpenseTransactionsScenarios() {
        return Stream.of(
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                                ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .account(Account.builder().id("accId").build())
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.EXPENSE)
                                        .account(Account.builder().id("accId").build())
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .account(Account.builder().id("accId").build())
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.EXPENSE)
                                        .account(Account.builder().id("accId").build())
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build()
                        ),
                        List.of()
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of()
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .amount(200.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(200.0)
                                        .build()
                        )
                )
        );
    }

    @ParameterizedTest(name = "Test get expense transactions. Given account: {1}, account's transactions: {2}. " +
            "Should return transactions list {2}")
    @MethodSource("getExpenseTransactionsScenarios")
    public void getExpenseTransactionsTest(Account account, List<Transaction> transactions,
                                           List<Transaction> expected) {
        account.setTransactions(transactions);
        for (Transaction transaction : transactions) {
            transaction.setAccount(account);
        }
        for (Transaction transaction : expected) {
            transaction.setAccount(account);
        }

        expect(transactionRepository.findAllByAccount(account)).andReturn(account.getTransactions());
        replay(transactionRepository);

        assertEquals(expected, transactionService.getExpenseTransactions(account, null, null));
        verify(transactionRepository);
    }

    private Stream<Arguments> getIncomeTransactionsScenarios() {
        return Stream.of(
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.INCOME)
                                        .build()
                                ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.INCOME)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .account(Account.builder().id("accId").build())
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.INCOME)
                                        .account(Account.builder().id("accId").build())
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .account(Account.builder().id("accId").build())
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.INCOME)
                                        .account(Account.builder().id("accId").build())
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build()
                        ),
                        List.of()
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        ),
                        List.of()
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build()
                        ),
                        List.of()
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build()
                        ),
                        List.of()
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .type(TransactionTypes.TRANSFER)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.COST)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .amount(200.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.INCOME)
                                        .amount(200.0)
                                        .build(),
                                Transaction.builder()
                                        .account(Account.builder().id("accId").build())
                                        .type(TransactionTypes.REVENUE)
                                        .amount(200.0)
                                        .build()
                        )
                )
        );
    }

    @ParameterizedTest(name = "Test get income transactions. Given account: {1}, account's transactions: {2}. " +
            "Should return transactions list {2}")
    @MethodSource("getIncomeTransactionsScenarios")
    public void getIncomeTransactionsTest(Account account, List<Transaction> transactions,
                                           List<Transaction> expected) {
        account.setTransactions(transactions);
        for (Transaction transaction : transactions) {
            transaction.setAccount(account);
        }
        for (Transaction transaction : expected) {
            transaction.setAccount(account);
        }

        expect(transactionRepository.findAllByAccount(account)).andReturn(account.getTransactions());
        replay(transactionRepository);

        assertEquals(expected, transactionService.getIncomeTransactions(account, null, null));
        verify(transactionRepository);
    }

    private Stream<Arguments> getRevenueTransactionsScenarios() {
        return Stream.of(
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.EXPENSE)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.TRANSFER)
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .build()
                        )
                )
        );
    }

    @ParameterizedTest(name = "Test get revenue transactions. Given account: {1}, account's transactions: {2}. " +
            "Should return transactions list {2}")
    @MethodSource("getRevenueTransactionsScenarios")
    public void getRevenueTransactionsTest(Account account, List<Transaction> transactions,
                                          List<Transaction> expected) {
        account.setTransactions(transactions);
        for (Transaction transaction : transactions) {
            transaction.setAccount(account);
        }
        for (Transaction transaction : expected) {
            transaction.setAccount(account);
        }

        expect(transactionRepository.findAllByAccount(account)).andReturn(account.getTransactions());
        replay(transactionRepository);

        assertEquals(expected, transactionService.getRevenueTransactions(account, null, null));
        verify(transactionRepository);
    }

    private Stream<Arguments> getCostTransactionsScenarios() {
        return Stream.of(
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .build()
                        )
                ),
                Arguments.of(
                        Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.INCOME)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.REVENUE)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.EXPENSE)
                                        .build(),
                                Transaction.builder()
                                        .type(TransactionTypes.TRANSFER)
                                        .receiver(Account.builder().id("anotherAccId").build())
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .type(TransactionTypes.COST)
                                        .build()
                        )
                )
        );
    }

    @ParameterizedTest(name = "Test get cost transactions. Given account: {1}, account's transactions: {2}. " +
            "Should return transactions list {2}")
    @MethodSource("getCostTransactionsScenarios")
    public void getCostTransactionsTest(Account account, List<Transaction> transactions,
                                          List<Transaction> expected) {
        account.setTransactions(transactions);
        for (Transaction transaction : transactions) {
            transaction.setAccount(account);
        }
        for (Transaction transaction : expected) {
            transaction.setAccount(account);
        }

        expect(transactionRepository.findAllByAccount(account)).andReturn(account.getTransactions());
        replay(transactionRepository);

        assertEquals(expected, transactionService.getCostTransactions(account, null, null));
        verify(transactionRepository);
    }

    private Stream<Arguments> getFindAllByAccountScenarios() {
        return Stream.of(
                Arguments.of(Account.builder().build(), ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(Account.builder().id("").build(), ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(Account.builder()
                        .id("accId")
                        .transactions(List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .amount(100.0)
                                        .build()
                        ))
                        .build(),
                        null)
        );
    }

    @ParameterizedTest(name = "Test find all transactions by account id. Given account: {0}. " +
            "Should throw an exception: {1} or return a list of transactions")
    @MethodSource("getFindAllByAccountScenarios")
    public void findAllByAccountTest(Account account, ErrorMessages exception) {
        expect(entityManager.find(Account.class, account.getId()))
                .andReturn(Strings.isBlank(account.getId()) ? null : account);
        Pageable pageable = PageRequest.of(1, 20);
        expect(transactionRepository.findAllPagesByAccount(account, pageable))
                .andReturn(account.getTransactions());
        replay(entityManager, transactionRepository);

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.findAllByAccount(account.getId(), null, null, 1));
            assertEquals(exception.name(), thrown.getMessage());
        } else {
            transactionService.findAllByAccount(account.getId(), null, null, 1);
            verify(entityManager, transactionRepository);
        }
    }

    private Stream<Arguments> getFindAllByCategories() {
        return Stream.of(
                Arguments.of(Account.builder().build(), List.of(), List.of(), false, ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(Account.builder().id("").build(), List.of(), List.of(), false, ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .category(Category.builder().name("Wage").build())
                                        .amount(100.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .category(Category.builder().name("Wage").build())
                                        .amount(100.0)
                                        .build()
                        ),
                        true,
                        null),
                Arguments.of(Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .amount(100.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .amount(100.0)
                                        .build()
                        ),
                        true,
                        null),
                Arguments.of(Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .amount(100.0)
                                        .build(),
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(100.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .amount(100.0)
                                        .build()
                        ),
                        true,
                        null),
                Arguments.of(Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(100.0)
                                        .category(Category.builder().name("Food").build())
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .category(Category.builder().name("Food").build())
                                        .amount(100.0)
                                        .build()
                        ),
                        false,
                        null),
                Arguments.of(Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(100.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(100.0)
                                        .build()
                        ),
                        false,
                        null),
                Arguments.of(Account.builder().id("accId").build(),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(100.0)
                                        .build(),
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.INCOME)
                                        .amount(100.0)
                                        .build()
                        ),
                        List.of(
                                Transaction.builder()
                                        .id("transactionId")
                                        .type(TransactionTypes.EXPENSE)
                                        .amount(100.0)
                                        .build()
                        ),
                        false,
                        null)
        );
    }

    @ParameterizedTest(name = "Test find all transactions by category id. Given account: {0}, all transactions: {1}, " +
            "is for income: {3}. Should throw an exception: {4} or return a list of expected transactions {2}")
    @MethodSource("getFindAllByCategories")
    public void findAllByCategoriesTest(Account account,
                                        List<Transaction> transactions,
                                        List<Transaction> expectedTransactions,
                                        boolean isIncome,
                                        ErrorMessages exception
    ) {
        account.setTransactions(transactions);
        for (Transaction transaction : transactions) {
            transaction.setAccount(account);
        }
        for (Transaction transaction : expectedTransactions) {
            transaction.setAccount(account);
        }

        expect(entityManager.find(Account.class, account.getId()))
                .andReturn(Strings.isBlank(account.getId()) ? null : account);
        expect(transactionRepository.findAllByAccount(account)).andReturn(transactions);
        replay(entityManager, transactionRepository);

        Map<Category, List<Transaction>> expected = expectedTransactions.stream()
                .collect(Collectors.groupingBy(transaction ->
                Optional.ofNullable(transaction.getCategory()).orElse(Category.builder().name("Other").build())
        ));

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.findAllByAccount(account.getId(), null, null, 1));
            assertEquals(exception.name(), thrown.getMessage());
        } else {
            assertEquals(expected,
                    transactionService.findAllByCategories(account.getId(), null, null, isIncome));

            verify(entityManager, transactionRepository);
        }
    }

}
