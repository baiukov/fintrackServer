package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.model.dto.AssetDto;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Currency;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountMapperTest {

    @Autowired
    private AccountMapper accountMapper;

    private Stream<Arguments> getMapperScenarios() {
        return Stream.of(
                Arguments.of(
                        AccountDto.builder().name("myAccount").build(),
                        Account.builder().id("accId").build(),
                        Account.builder().id("accId").name("myAccount").build()
                ),
                Arguments.of(
                        AccountDto.builder().build(),
                        Account.builder().id("accId").name("myAccount").build(),
                        Account.builder().id("accId").name("myAccount").build()
                ),
                Arguments.of(
                        AccountDto.builder().name("newAccName").build(),
                        Account.builder().id("assetId").name("myAccount").build(),
                        Account.builder().id("assetId").name("newAccName").build()
                ),
                Arguments.of(
                        AccountDto.builder()
                                .name("myAccount")
                                .interestRate(1.1)
                                .type(AccountType.SAVINGS_ACCOUNT)
                                .currency(Currency.getInstance("EUR"))
                                .initialAmount(1000.0)
                                .build(),
                        Account.builder().id("accId").build(),
                        Account.builder().id("accId")
                                .name("myAccount")
                                .interestRate(1.1)
                                .type(AccountType.SAVINGS_ACCOUNT)
                                .currency(Currency.getInstance("EUR"))
                                .initialAmount(1000.0)
                                .build()
                ),
                Arguments.of(
                        AccountDto.builder()
                                .name("defaultAcc")
                                .interestRate(0.0)
                                .type(AccountType.CURRENT_ACCOUNT)
                                .currency(Currency.getInstance("USD"))
                                .initialAmount(500.0)
                                .build(),
                        Account.builder().id("accId")
                                .name("myAccount")
                                .interestRate(1.1)
                                .type(AccountType.SAVINGS_ACCOUNT)
                                .currency(Currency.getInstance("EUR"))
                                .initialAmount(1000.0)
                                .build(),
                        Account.builder().id("accId")
                                .name("defaultAcc")
                                .interestRate(0)
                                .type(AccountType.CURRENT_ACCOUNT)
                                .currency(Currency.getInstance("USD"))
                                .initialAmount(500.0)
                                .build()
                )
        );
    }


    @ParameterizedTest(name = "Account mapper test. Given dto: {0}, oldAccount: {1}." +
            " Should return account {2}")
    @MethodSource("getMapperScenarios")
    public void updateAccountFromDtoTest(AccountDto dto, Account oldAccount, Account newAccount) {
        accountMapper.updateAccountFromDto(dto, oldAccount);
        assertEquals(newAccount, oldAccount);
    }
}
