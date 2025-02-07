package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.enums.UserRights;
import me.vse.fintrackserver.mappers.AccountMapper;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    private EntityManager entityManager;
    private AccountRepository accountRepository;
    private AccountMapper accountMapper;
    private AssetService assetService;
    private TransactionService transactionService;
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        entityManager = EasyMock.mock(EntityManager.class);
        accountRepository = EasyMock.mock(AccountRepository.class);
        accountMapper = EasyMock.mock(AccountMapper.class);
        assetService = EasyMock.mock(AssetService.class);
        transactionService = EasyMock.mock(TransactionService.class);
        accountService = AccountService.builder()
                .entityManager(entityManager)
                .accountRepository(accountRepository)
                .accountMapper(accountMapper)
                .assetService(assetService)
                .transactionService(transactionService)
                .build();
    }

    private Stream<Arguments> getAccountAddScenarios() {
        return Stream.of(
                Arguments.of(false, "accName", "USD",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(true, null, "USD",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.ACCOUNT_NAME_IS_BLANK.name()),
                Arguments.of(true, "", "USD",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.ACCOUNT_NAME_IS_BLANK.name()),
                Arguments.of(true, "accName", "ABC",
                        AccountType.CURRENT_ACCOUNT.name(), null),
                Arguments.of(false, null, "USD",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(false, null, "123",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(true, "", "123",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.ACCOUNT_NAME_IS_BLANK.name()),
                Arguments.of(false, "accName", "ABC",
                        AccountType.CURRENT_ACCOUNT.name(), ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(true, "accName", "USD", AccountType.CURRENT_ACCOUNT.name(), ""),
                Arguments.of(true, "newacc", "USD", AccountType.SAVINGS_ACCOUNT.name(), ""),
                Arguments.of(true, "accNum2", "EUR", AccountType.LOAN.name(), "")
        );
    }

    @ParameterizedTest(name = "Test add new account. Given does user exist: {0}, account name: {1}, currency: {2}, " +
            "account type: {3}. Should return error message: {4}")
    @MethodSource("getAccountAddScenarios")
    public void addTest(boolean doesUserExist, String accountName, String currency,
                        String accountType, String exceptionMessage) {
        User owner = User.builder().id("id").build();
        expect(entityManager.find(anyObject(), anyString())).andReturn(doesUserExist ? owner : null);
        entityManager.persist(anyObject(Account.class));
        entityManager.persist(anyObject(Account.class));
        replay(entityManager);

        AccountAddRequest request = AccountAddRequest.builder()
                .ownerId("id")
                .name(accountName)
                .currency(currency)
                .type(accountType)
                .build();

        if (!"".equals(exceptionMessage)) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    accountService.add(request));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            Account expected = Account.builder()
                    .name(accountName)
                    .currency(Currency.getInstance(currency))
                    .type(AccountType.valueOf(accountType))
                    .build();
            assertEquals(expected, accountService.add(request));
            verify(entityManager);
        }
    }

    @Test
    public void retrieveAllTest() {
        User user = User.builder().id("userId").build();
        User user2 = User.builder().id("userId2").build();

        Account account1 = Account.builder().id("acc1").build();
        Account account2 = Account.builder().id("acc2").build();
        Account account3 = Account.builder().id("acc3").build();
        Account account4 = Account.builder().id("acc4").build();

        AccountUserRights accountUserRights1 = AccountUserRights.builder().user(user).account(account1)
                .rights(UserRights.WRITE).isOwner(true).build();
        AccountUserRights accountUserRights2 = AccountUserRights.builder().user(user).account(account2)
                .rights(UserRights.WRITE).isOwner(false).build();


        AccountUserRights accountUserRights3 = AccountUserRights.builder().user(user).account(account3)
                .rights(UserRights.READ).isOwner(false).build();
        AccountUserRights accountUserRights4 = AccountUserRights.builder().user(user2).account(account3)
                .rights(UserRights.WRITE).isOwner(true).build();

        AccountUserRights accountUserRights5 = AccountUserRights.builder().user(user).account(account4)
                .rights(UserRights.WRITE).isOwner(true).build();
        AccountUserRights accountUserRights6 = AccountUserRights.builder().user(user2).account(account4)
                .rights(UserRights.READ).isOwner(false).build();

        user.setAccountUserRights(List.of(
                accountUserRights1, accountUserRights2, accountUserRights3, accountUserRights5
        ));

        user2.setAccountUserRights(List.of(accountUserRights3, accountUserRights4, accountUserRights6));

        expect(entityManager.find(anyObject(), anyString())).andReturn(user);
        replay(entityManager);

        List<Account> userAccounts = List.of(account1, account2, account3, account4);

        assertEquals(userAccounts, accountService.retrieveAll(user.getId()));

    }

    @Test
    public void updateTest() {
        // nothing to test
    }

    private Stream<Arguments> getCheckNetWorthScenarios() {
        return Stream.of(
                Arguments.of(
                        true, Account.builder().build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        1600.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        2620,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        1600.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        2620,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        1600.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                )
                        ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        2620,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        1600.0,
                        null
                ),

                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                )
                        ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.COST).amount(100.0).build()
                                )
                        ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                        )
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build())
                                ).build(),
                        580.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(200.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(500.0).build())
                                ).build(),
                        580.0,
                        null
                )
        );
    }

    private Stream<Arguments> getIncomeScenarios() {
        return Stream.of(
                Arguments.of(
                        true, Account.builder().build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1100,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1100,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1100,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1100,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1100.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1100.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        1100.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1100.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),

                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.COST).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                        )
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build())
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(200.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(500.0).build())
                                ).build(),
                        200.0,
                        null
                )
        );
    }

    @ParameterizedTest(name = "Check net worth test. Given does account exist: {0}, account: {1}, " +
            "expected amount: {2}, exception message: {3}")
    @MethodSource("getCheckNetWorthScenarios")
    public void getNetWorthTest(boolean doesAccExist, Account account, double expected,
                                  String exceptionMessage
    ) {
        expect(entityManager.find(Account.class, account.getId())).andReturn(doesAccExist ? account : null);
        expect(assetService.getCurrentAssetPrice(isA(Asset.class))).andAnswer(() -> {
            Asset asset = (Asset) getCurrentArguments()[0];
            return asset.getAcquisitionPrice();
        }).anyTimes();

        if (account.getTransactions() != null) {
            expect(transactionService.getRevenueTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.REVENUE.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
            expect(transactionService.getIncomeTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.INCOME.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
            expect(transactionService.getCostTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.COST.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
            expect(transactionService.getExpenseTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.EXPENSE.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
        } else {
            expect(transactionService.getRevenueTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
            expect(transactionService.getIncomeTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
            expect(transactionService.getCostTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
            expect(transactionService.getExpenseTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
        }


        replay(entityManager, assetService, transactionService);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    accountService.getNetWorth(account.getId(), null, null));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            assertEquals(expected, accountService.getNetWorth(account.getId(), null, null));
        }
    }

    @ParameterizedTest(name = "Check get income. Given does account exist: {0}, account: {1}, " +
            "expected amount: {2}, exception message: {3}")
    @MethodSource("getIncomeScenarios")
    public void getIncomeTest(boolean doesAccExist, Account account, double expected,
                                  String exceptionMessage
    ) {
        expect(entityManager.find(Account.class, account.getId())).andReturn(doesAccExist ? account : null);
        expect(assetService.getCurrentAssetPrice(isA(Asset.class))).andAnswer(() -> {
            Asset asset = (Asset) getCurrentArguments()[0];
            return asset.getAcquisitionPrice();
        }).anyTimes();

        if (account.getTransactions() != null) {
            expect(transactionService.getIncomeTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.INCOME.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
        } else {
            expect(transactionService.getIncomeTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
        }


        replay(entityManager, assetService, transactionService);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    accountService.getIncome(account.getId(), null, null));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            assertEquals(expected, accountService.getIncome(account.getId(), null, null));
        }
    }


    private Stream<Arguments> getExpenseScenarios() {
        return Stream.of(
                Arguments.of(
                        true, Account.builder().build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                )
                        ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        -80,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),

                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.COST).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                        )
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build())
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(200.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(500.0).build())
                                ).build(),
                        -500.0,
                        null
                )
        );
    }
    @ParameterizedTest(name = "Check expense test. Given does account exist: {0}, account: {1}, " +
            "expected amount: {2}, exception message: {3}")
    @MethodSource("getExpenseScenarios")
    public void getExpenseTest(boolean doesAccExist, Account account, double expected,
                                  String exceptionMessage
    ) {
        expect(entityManager.find(Account.class, account.getId())).andReturn(doesAccExist ? account : null);
        expect(assetService.getCurrentAssetPrice(isA(Asset.class))).andAnswer(() -> {
            Asset asset = (Asset) getCurrentArguments()[0];
            return asset.getAcquisitionPrice();
        }).anyTimes();

        if (account.getTransactions() != null) {
            expect(transactionService.getExpenseTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.EXPENSE.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
        } else {
            expect(transactionService.getExpenseTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
        }


        replay(entityManager, assetService, transactionService);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    accountService.getExpense(account.getId(), null, null));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            assertEquals(expected, accountService.getExpense(account.getId(), null, null));
        }
    }

    private Stream<Arguments> getBalanceScenarios() {
        return Stream.of(
                Arguments.of(
                        true, Account.builder().build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        false, Account.builder().id("accId").build(), 0, ErrorMessages.ACCOUNT_DOESNT_EXIST.name()
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.CURRENT_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        150,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                        )
                                ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                        )
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.SAVINGS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        150,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(100.0).build()
                                )
                        ).build(),
                        -150.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                        Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build()
                                )
                        ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.LOAN)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(100.0).build())
                                ).build(),
                        1020,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(
                                List.of(
                                        Asset.builder().acquisitionPrice(1000.0).build(),
                                        Asset.builder().acquisitionPrice(100.0).build(),
                                        Asset.builder().acquisitionPrice(500.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),

                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>()).transactions(
                                List.of(
                                        Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                        Transaction.builder().type(TransactionTypes.COST).amount(100.0).build()
                                )
                        ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT).assets(new ArrayList<>())
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.COST).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(100.0).build()
                                        )
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build())
                                ).build(),
                        0.0,
                        null
                ),
                Arguments.of(
                        true,
                        Account.builder().id("accId").type(AccountType.BUSINESS_ACCOUNT)
                                .assets(
                                        List.of(
                                                Asset.builder().acquisitionPrice(1000.0).build(),
                                                Asset.builder().acquisitionPrice(100.0).build(),
                                                Asset.builder().acquisitionPrice(500.0).build()))
                                .transactions(
                                        List.of(
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(50.0).build(),
                                                Transaction.builder().type(TransactionTypes.REVENUE).amount(30.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(1000.0).build(),
                                                Transaction.builder().type(TransactionTypes.COST).amount(100.0).build(),
                                                Transaction.builder().type(TransactionTypes.INCOME).amount(200.0).build(),
                                                Transaction.builder().type(TransactionTypes.EXPENSE).amount(500.0).build())
                                ).build(),
                        -300,
                        null
                )
        );
    }
    @ParameterizedTest(name = "Get balance test. Given does account exist: {0}, account: {1}, " +
            "expected amount: {2}, exception message: {3}")
    @MethodSource("getBalanceScenarios")
    public void getBalanceTest(boolean doesAccExist, Account account, double expected,
                                  String exceptionMessage
    ) {
        expect(entityManager.find(Account.class, account.getId())).andReturn(doesAccExist ? account : null).anyTimes();
        expect(assetService.getCurrentAssetPrice(isA(Asset.class))).andAnswer(() -> {
            Asset asset = (Asset) getCurrentArguments()[0];
            return asset.getAcquisitionPrice();
        }).anyTimes();

        if (account.getTransactions() != null) {
            expect(transactionService.getExpenseTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.EXPENSE.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
            expect(transactionService.getIncomeTransactions(account, null, null)).andReturn(
                    account.getTransactions().stream()
                            .filter(Objects::nonNull)
                            .filter(transaction -> TransactionTypes.INCOME.equals(transaction.getType()))
                            .collect(Collectors.toList())
            ).anyTimes();
        } else {
            expect(transactionService.getExpenseTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
            expect(transactionService.getIncomeTransactions(account, null, null))
                    .andReturn(new ArrayList<>()).anyTimes();
        }


        replay(entityManager, assetService, transactionService);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    accountService.getBalance(account.getId(), null, null));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            assertEquals(expected, accountService.getBalance(account.getId(), null, null));
        }
    }
}
