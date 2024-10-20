package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.DatabaseTest;
import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.repositories.UserRepository;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class AccountServiceTest  {

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Autowired
    private AccountService accountService;

    @Test
    public void test() {
        accountService.add(new AccountAddRequest());
    }

    @Test
    public void test2() {
    }

}
