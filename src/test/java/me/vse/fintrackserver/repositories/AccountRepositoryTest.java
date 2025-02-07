package me.vse.fintrackserver.repositories;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    void findByNameTest() {
        Account account = Account.builder()
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .name("myAccount").build();
        Account account2 = Account.builder()
                .type(AccountType.CURRENT_ACCOUNT)
                .currency(Currency.getInstance("USD"))
                .name("anotherAcc").build();
        accountRepository.saveAll(List.of(account, account2));

        Account actual = accountRepository.findByName("myAccount");
        assertEquals(account, actual);
    }
}
