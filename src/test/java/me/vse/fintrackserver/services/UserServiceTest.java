package me.vse.fintrackserver.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.ATest;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.repositories.UserRepository;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends ATest {

    private EntityManager entityManager;
    private UserRepository userRepository;

    private UserService userService;


    @BeforeEach
    public void setUp() {
        entityManager = EasyMock.mock(EntityManager.class);
        userRepository = EasyMock.mock(UserRepository.class);
        userService = new UserService(userRepository, entityManager);
    }

    private Stream<Arguments> getRegisterUserScenarios() {
        return Stream.of(
                Arguments.of("test@gmail.com", "newUser", "1234567Aa"),
                Arguments.of("test2@seznam.cz", "random", "superStrongPassword123"),
                Arguments.of("strange@mail.e", "strangeUser", "strangePassword32")
        );
    }


    @ParameterizedTest(name = "given email: {0} username: {1}, password: {2}. Should return new user instance")
    @MethodSource("getRegisterUserScenarios")
    public void registerUser(String email, String userName, String password) {
        entityManager.persist(anyObject(User.class));
        expect(userRepository.findByUserName(anyString())).andReturn(null);
        expect(userRepository.findByEmail(anyString())).andReturn(null);
        replay(entityManager, userRepository);
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User expectedUser = User.builder().email(email).userName(userName).password(hashedPassword).build();

        User user = userService.registerUser(email, userName, password);

        assertEquals(expectedUser, user);
        verify(entityManager);
    }

    private Stream<Arguments> getPasswordValidationScenarios() {
        return Stream.of(
                Arguments.of("1234", ErrorMessages.PASSWORD_LESS_THAN_8_CHARS.name()),
                Arguments.of("12345678", ErrorMessages.PASSWORD_DOESNT_CONTAIN_UPPERCASE_LETTER.name()),
                Arguments.of("12345678A", ErrorMessages.PASSWORD_DOESNT_CONTAIN_LOWERCASE_LETTER.name()),
                Arguments.of("Abcdefghjkl", ErrorMessages.PASSWORD_DOESNT_CONTAIN_DIGIT.name()),
                Arguments.of("1234 5678Aa", ErrorMessages.PASSWORD_CONTAINS_WHITESPACES.name()),
                Arguments.of("12345678Aa", null)
        );


    }

    @ParameterizedTest(name = "Test password validation. Given password: {0}. Should throw exeption message: {1}")
    @MethodSource("getPasswordValidationScenarios")
    public void passwordValidateTest(String givenPassword, String expectedExceptionMessage) {
        expect(userRepository.findByUserName(anyString())).andReturn(null);
        expect(userRepository.findByEmail(anyString())).andReturn(null);
        replay(userRepository);

        Executable call = () -> userService.registerUser("test@gmail.com", "random", givenPassword);

        if (expectedExceptionMessage == null) {
            assertDoesNotThrow(call);
        } else {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, call, expectedExceptionMessage);
            assertEquals(expectedExceptionMessage, thrown.getMessage());
        }

    }

    @ParameterizedTest(name = "Test user exists check. Given email registered: {0}, user name registered: {1}")
    @CsvSource(value = {
            "true | true | true",
            "false | true | true",
            "false | false | false"
    }, delimiter = '|')
    public void userExistsCheckTest(boolean isEmailRegistered, boolean isUserNameRegistered,
                                    boolean shouldThrowException
    ) {
      expect(userRepository.findByEmail(anyString())).andReturn(isEmailRegistered ? randomString(1) : null);
      expect(userRepository.findByUserName(anyString())).andReturn(isUserNameRegistered ? randomString(1) : null);
      replay(userRepository);

      Executable call = () -> userService.registerUser("test@gmail.com", "random", "12345678Aa");

      if (shouldThrowException) {
          assertThrows(IllegalArgumentException.class, call);
      } else {
          assertDoesNotThrow(call);
      }
    }
}
