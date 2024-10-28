package me.vse.fintrackserver.repositories;

import at.favre.lib.crypto.bcrypt.BCrypt;
import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static me.vse.fintrackserver.ATest.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void findByUserNameTest() {
        User user1 = User.builder()
                .userName("bestUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("bestUser@gmail.com")
                .build();
        User user2 = User.builder()
                .userName("anotherGoodUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("anotherGoodUser@gmail.com")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        assertEquals(user1.getId(), userRepository.findByUserName("bestUser"));
    }

    @Test
    void findByEmailTest() {
        User user1 = User.builder()
                .userName("bestUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("bestUser@gmail.com")
                .build();
        User user2 = User.builder()
                .userName("anotherGoodUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("anotherGoodUser@gmail.com")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        assertEquals(user1.getId(), userRepository.findByEmail("bestUser@gmail.com"));
    }

    @Test
    void findByUserNameOrEmailTest() {
        User user1 = User.builder()
                .userName("bestUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("bestUser@gmail.com")
                .build();
        User user2 = User.builder()
                .userName("bestUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("anotherGoodUser@gmail.com")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        assertEquals(user1.getId(), userRepository.findByEmail("bestUser@gmail.com"));
    }

    @Test
    void findUsersTest() {
        User user1 = User.builder()
                .userName("bestUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("bestUser@gmail.com")
                .build();
        User user2 = User.builder()
                .userName("anotherGoodUser")
                .password(BCrypt.withDefaults().hashToString(12, "secretPassword123".toCharArray()))
                .email("anotherGoodUser@gmail.com")
                .build();
        userRepository.saveAll(List.of(user1, user2));
        List<String> ids = userRepository.findAll()
                        .stream()
                        .map(User::getId)
                        .toList();

        assertEquals(List.of(user1, user2), userRepository.findUsers(ids));
    }

    @Test
    void findAllPageableTest() {
        PageRequest request = PageRequest.of(0, 5);
        List<User> expected = new ArrayList<>();
        List<User> allUsers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .userName(randomString(5))
                    .password(BCrypt.withDefaults().hashToString(12, randomString(15).toCharArray()))
                    .email(randomString(25))
                    .build();

            if (expected.size() < 5) expected.add(user);
            allUsers.add(user);
        }
        userRepository.saveAll(allUsers);

        assertTrue(userRepository.findAllPageable(request).containsAll(expected));
    }
}
