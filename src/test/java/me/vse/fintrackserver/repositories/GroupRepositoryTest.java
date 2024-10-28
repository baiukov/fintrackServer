package me.vse.fintrackserver.repositories;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.enums.AccountType;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @AfterEach
    void tearDown() {
        groupRepository.deleteAll();
    }

    @Test
    void findByCodeTest() {
        Group group = Group.builder()
                .name("Group1")
                .code("GRP111")
                .build();
        Group group2 = Group.builder()
                .name("Group2")
                .code("GRP222")
                .build();
        groupRepository.saveAll(List.of(group, group2));

        Group actual = groupRepository.findByCode("GRP111");
        assertEquals(group, actual);
    }
}
