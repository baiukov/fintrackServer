package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static me.vse.fintrackserver.ATest.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void findAllByAccountTest() {
        Account account = Account.builder()
                .name("myAccount")
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .build();
        Account anotherAccount = Account.builder()
                .name("anotherAccount")
                .type(AccountType.SAVINGS_ACCOUNT)
                .currency(Currency.getInstance("EUR"))
                .build();
        accountRepository.saveAll(List.of(account, anotherAccount));
        Transaction transaction1 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .build();
        Transaction transaction2 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.EXPENSE)
                .amount(250.0)
                .lat(45.0)
                .lon(45.0)
                .build();
        Transaction transaction3 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.REVENUE)
                .amount(1000.0)
                .photo("money.png")
                .build();
        Transaction transaction4 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .build();
        Transaction transaction5 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.COST)
                .amount(100.0)
                .build();
        Transaction transaction6 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.TRANSFER)
                .receiver(anotherAccount)
                .amount(200.0)
                .build();
        Transaction transaction7 = Transaction.builder()
                .account(anotherAccount)
                .receiver(account)
                .type(TransactionTypes.TRANSFER)
                .amount(150.0)
                .build();
        Transaction transaction8 = Transaction.builder()
                .account(anotherAccount)
                .type(TransactionTypes.EXPENSE)
                .amount(500.0)
                .build();

        transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3, transaction4,
                transaction5, transaction6, transaction7, transaction8));

        List<Transaction> expected = List.of(transaction1, transaction2, transaction3, transaction4,
                transaction5, transaction6, transaction7);

        assertEquals(expected, transactionRepository.findAllByAccount(account));
    }


    @Test
    void findAllByAccountTest2() {
        LocalDateTime fromDate = LocalDateTime.of(2024, 9, 10, 12, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2024, 10, 12, 12, 0, 0);

        Account account = Account.builder()
                .name("myAccount")
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .build();
        Account anotherAccount = Account.builder()
                .name("anotherAccount")
                .type(AccountType.SAVINGS_ACCOUNT)
                .currency(Currency.getInstance("EUR"))
                .build();
        accountRepository.saveAll(List.of(account, anotherAccount));
        Transaction transaction1 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 9, 11, 12, 0))
                .build();
        Transaction transaction2 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.EXPENSE)
                .amount(250.0)
                .lat(45.0)
                .lon(45.0)
                .executionDateTime(LocalDateTime.of(2024, 9, 15, 12, 0))
                .build();
        Transaction transaction3 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.REVENUE)
                .amount(1000.0)
                .photo("money.png")
                .executionDateTime(LocalDateTime.of(2024, 9, 18, 16, 0))
                .build();
        Transaction transaction4 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 1, 20, 0))
                .build();
        Transaction transaction5 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.COST)
                .amount(100.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 2, 7, 40))
                .build();
        Transaction transaction6 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.TRANSFER)
                .receiver(anotherAccount)
                .amount(200.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 2, 7, 40))
                .build();
        Transaction transaction7 = Transaction.builder()
                .account(anotherAccount)
                .receiver(account)
                .type(TransactionTypes.TRANSFER)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 6, 12, 40))
                .build();
        Transaction transaction8 = Transaction.builder()
                .account(anotherAccount)
                .type(TransactionTypes.EXPENSE)
                .amount(500.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 12, 11, 0))
                .build();

        List<Transaction> transactionsWithOtherDates = List.of(
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.INCOME)
                        .amount(150.0)
                        .executionDateTime(LocalDateTime.of(2024, 8, 11, 12, 0))
                        .build(),
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.EXPENSE)
                        .amount(250.0)
                        .lat(45.0)
                        .lon(45.0)
                        .executionDateTime(LocalDateTime.of(2024, 8, 15, 12, 0))
                        .build(),
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.REVENUE)
                        .amount(1000.0)
                        .photo("money.png")
                        .executionDateTime(LocalDateTime.of(2024, 8, 18, 16, 0))
                        .build(),
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.INCOME)
                        .amount(150.0)
                        .executionDateTime(LocalDateTime.of(2023, 10, 1, 20, 0))
                        .build(),
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.COST)
                        .amount(100.0)
                        .executionDateTime(LocalDateTime.of(2024, 11, 2, 7, 40))
                        .build(),
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.TRANSFER)
                        .receiver(anotherAccount)
                        .amount(200.0)
                        .executionDateTime(LocalDateTime.of(2025, 10, 2, 7, 40))
                        .build(),
                Transaction.builder()
                        .account(anotherAccount)
                        .receiver(account)
                        .type(TransactionTypes.TRANSFER)
                        .amount(150.0)
                        .executionDateTime(LocalDateTime.of(2024, 10, 12, 16, 40))
                        .build()
        );

        transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3, transaction4,
                transaction5, transaction6, transaction7, transaction8));
        transactionRepository.saveAll(transactionsWithOtherDates);

        List<Transaction> expected = List.of(transaction1, transaction2, transaction3, transaction4,
                transaction5, transaction6, transaction7);

        assertEquals(expected, transactionRepository.findAllByAccount(account, fromDate, toDate));
    }

    @Test
    void findAllByAccountTest3() {
        LocalDateTime toDate = LocalDateTime.of(2024, 10, 12, 12, 0, 0);

        Account account = Account.builder()
                .name("myAccount")
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .build();
        Account anotherAccount = Account.builder()
                .name("anotherAccount")
                .type(AccountType.SAVINGS_ACCOUNT)
                .currency(Currency.getInstance("EUR"))
                .build();
        accountRepository.saveAll(List.of(account, anotherAccount));
        Transaction transaction1 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 9, 11, 12, 0))
                .build();
        Transaction transaction2 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.EXPENSE)
                .amount(250.0)
                .lat(45.0)
                .lon(45.0)
                .executionDateTime(LocalDateTime.of(2024, 9, 15, 12, 0))
                .build();
        Transaction transaction3 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.REVENUE)
                .amount(1000.0)
                .photo("money.png")
                .executionDateTime(LocalDateTime.of(2024, 9, 18, 16, 0))
                .build();
        Transaction transaction4 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 1, 20, 0))
                .build();
        Transaction transaction5 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.COST)
                .amount(100.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 2, 7, 40))
                .build();
        Transaction transaction6 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.TRANSFER)
                .receiver(anotherAccount)
                .amount(200.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 2, 7, 40))
                .build();
        Transaction transaction7 = Transaction.builder()
                .account(anotherAccount)
                .receiver(account)
                .type(TransactionTypes.TRANSFER)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 6, 12, 40))
                .build();
        Transaction transaction8 = Transaction.builder()
                .account(anotherAccount)
                .type(TransactionTypes.EXPENSE)
                .amount(500.0)
                .executionDateTime(LocalDateTime.of(2024, 10, 12, 11, 0))
                .build();
        Transaction transaction9 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2024, 8, 11, 12, 0))
                .build();
        Transaction transaction10 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.EXPENSE)
                .amount(250.0)
                .lat(45.0)
                .lon(45.0)
                .executionDateTime(LocalDateTime.of(2024, 8, 15, 12, 0))
                .build();
        Transaction transaction11 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.REVENUE)
                .amount(1000.0)
                .photo("money.png")
                .executionDateTime(LocalDateTime.of(2024, 8, 18, 16, 0))
                .build();
        Transaction transaction12 = Transaction.builder()
                .account(account)
                .type(TransactionTypes.INCOME)
                .amount(150.0)
                .executionDateTime(LocalDateTime.of(2023, 10, 1, 20, 0))
                .build();

        List<Transaction> transactionsWithOtherDates = List.of(
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.COST)
                        .amount(100.0)
                        .executionDateTime(LocalDateTime.of(2024, 11, 2, 7, 40))
                        .build(),
                Transaction.builder()
                        .account(account)
                        .type(TransactionTypes.TRANSFER)
                        .receiver(anotherAccount)
                        .amount(200.0)
                        .executionDateTime(LocalDateTime.of(2025, 10, 2, 7, 40))
                        .build(),
                Transaction.builder()
                        .account(anotherAccount)
                        .receiver(account)
                        .type(TransactionTypes.TRANSFER)
                        .amount(150.0)
                        .executionDateTime(LocalDateTime.of(2024, 10, 12, 16, 40))
                        .build()
        );

        transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3, transaction4,
                transaction5, transaction6, transaction7, transaction8, transaction9, transaction10, transaction11,
                transaction12));
        transactionRepository.saveAll(transactionsWithOtherDates);

        List<Transaction> expected = List.of(transaction1, transaction2, transaction3, transaction4,
                transaction5, transaction6, transaction7, transaction9, transaction10, transaction11,
                transaction12);

        assertEquals(expected, transactionRepository.findAllByAccount(account, toDate));
    }

    @Test
    void findAllPagesByAccountTest() {
        List<Transaction> expected = new ArrayList<>();
        List<Transaction> allTransactions = new ArrayList<>();

        Account account = Account.builder().name("myAccount")
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .build();
        Account anotherAcc = Account.builder().name("anotherAccount")
                .type(AccountType.SAVINGS_ACCOUNT)
                .currency(Currency.getInstance("CZK"))
                .build();
        accountRepository.saveAll(List.of(account, anotherAcc));

        int batchSize = 5;
        for (int i = 0; i < 10; i++) {
            boolean isForMainAcc = Math.random() * 99 > 50;
            Transaction transaction = Transaction.builder()
                    .account(isForMainAcc ? account : anotherAcc)
                    .note(randomString(20))
                    .type(TransactionTypes.EXPENSE)
                    .amount(Math.floor(Math.random() * 500))
                    .build();

            if (isForMainAcc && expected.size() < batchSize) expected.add(transaction);
            allTransactions.add(transaction);
        }
        transactionRepository.saveAll(allTransactions);
        transactionRepository.flush();

        PageRequest request = PageRequest.of(0, batchSize);
        List<Transaction> actual = transactionRepository.findAllPagesByAccount(account, request);

        assertTrue(actual.containsAll(expected));
    }

    @Test
    void findAllPagesByAccount2() {
        List<Transaction> expected = new ArrayList<>();
        List<Transaction> allTransactions = new ArrayList<>();
        LocalDateTime fromDate = LocalDateTime.of(2024, 9, 10, 12, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2024, 10, 12, 12, 0, 0);

        Account account = Account.builder().name("myAccount")
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .build();
        Account anotherAcc = Account.builder().name("anotherAccount")
                .type(AccountType.SAVINGS_ACCOUNT)
                .currency(Currency.getInstance("CZK"))
                .build();
        accountRepository.saveAll(List.of(account, anotherAcc));

        int batchSize = 5;
        for (int i = 0; i < 10; i++) {
            boolean isForMainAcc = Math.random() * 99 > 50;
            boolean isBetweenDays = Math.random() * 99 > 50;
            Transaction transaction = Transaction.builder()
                    .account(isForMainAcc ? account : anotherAcc)
                    .note(randomString(20))
                    .type(TransactionTypes.EXPENSE)
                    .amount(Math.floor(Math.random() * 500))
                    .executionDateTime(isBetweenDays ? fromDate.plusDays(1) : fromDate.minusDays(5))
                    .build();

            if (isForMainAcc && expected.size() < batchSize && isBetweenDays) expected.add(transaction);
            allTransactions.add(transaction);
        }
        transactionRepository.saveAll(allTransactions);
        transactionRepository.flush();

        PageRequest request = PageRequest.of(0, batchSize);
        List<Transaction> actual = transactionRepository.findAllPagesByAccount(account, fromDate, toDate, request);

        assertTrue(actual.containsAll(expected));
    }

    @Test
    void findAllPagesByAccount3() {
        List<Transaction> expected = new ArrayList<>();
        List<Transaction> allTransactions = new ArrayList<>();
        LocalDateTime toDate = LocalDateTime.of(2024, 10, 12, 12, 0, 0);

        Account account = Account.builder().name("myAccount")
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .build();
        Account anotherAcc = Account.builder().name("anotherAccount")
                .type(AccountType.SAVINGS_ACCOUNT)
                .currency(Currency.getInstance("CZK"))
                .build();
        accountRepository.saveAll(List.of(account, anotherAcc));

        int batchSize = 5;
        for (int i = 0; i < 10; i++) {
            boolean isForMainAcc = Math.random() * 99 > 50;
            boolean isBetweenDays = Math.random() * 99 > 50;
            Transaction transaction = Transaction.builder()
                    .account(isForMainAcc ? account : anotherAcc)
                    .note(randomString(20))
                    .type(TransactionTypes.EXPENSE)
                    .amount(Math.floor(Math.random() * 500))
                    .executionDateTime(isBetweenDays ? toDate.minusDays(1) : toDate.plusDays(5))
                    .build();

            if (isForMainAcc && expected.size() < batchSize && isBetweenDays) expected.add(transaction);
            allTransactions.add(transaction);
        }
        transactionRepository.saveAll(allTransactions);
        transactionRepository.flush();

        PageRequest request = PageRequest.of(0, batchSize);
        List<Transaction> actual = transactionRepository.findAllPagesByAccount(account, toDate, request);

        assertTrue(actual.containsAll(expected));
    }
}
