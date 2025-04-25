package me.vse.fintrackserver.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.ATest;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.repositories.CategoryRepository;
import me.vse.fintrackserver.repositories.UserRepository;
import me.vse.fintrackserver.rest.responses.UserAuthResponse;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends ATest {

    private EntityManager entityManager;
    private UserRepository userRepository;
    private UserService userService;
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setUp() {
        entityManager = EasyMock.mock(EntityManager.class);
        userRepository = EasyMock.mock(UserRepository.class);
        categoryRepository = EasyMock.mock(CategoryRepository.class);
        userService = new UserService(userRepository, entityManager, categoryRepository, null, null, null, null, "", "");
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
    public void registerUserTest(String email, String userName, String password) {
        entityManager.persist(anyObject(User.class));
        expect(userRepository.findByUserName(anyString())).andReturn(null);
        expect(userRepository.findByEmail(anyString())).andReturn(null);
        replay(entityManager, userRepository);
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User expectedUser = User.builder().email(email).userName(userName).password(hashedPassword).build();

        UserAuthResponse user = userService.registerUser(email, userName, password);

        assertEquals(expectedUser.getId(), user.getId());
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

    private Stream<Arguments> getLoginScenarios() {
        return Stream.of(
                Arguments.of(false, "12345678Aa", "12345678Aa", ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(false, "12345678Aa", "notCorrect", ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(true, "12345678Aa", "notCorrect", ErrorMessages.WRONG_PASSWORD.name()),
                Arguments.of(true, "12345678Aa", "12345678Aa", null)
        );
    }

    @ParameterizedTest(name = "Test login. Given is email or username registered: {0}, registered password {1}, " +
            "given password: {2}. Should throw exception: {3}")
    @MethodSource("getLoginScenarios")
    public void loginTest(boolean isUserFound, String expectedPassword, String givenPassword, String exceptionMessage) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, expectedPassword.toCharArray());
        User user = User.builder().email("test@gmail.com").userName("user").password(hashedPassword).build();

        expect(userRepository.findByUserNameOrEmail(anyString(), anyString())).andReturn(isUserFound ? user : null);
        replay(userRepository);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    userService.login("test@gmail.com", "user", givenPassword)
            );
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            assertEquals(user, userService.login("test@gmail.com", "user", givenPassword));
        }

        verify(userRepository);
    }

    public Stream<Arguments> getSetPincodeTestScenarios() {
        return Stream.of(
                Arguments.of(null, "id", "1234", ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of("id", "inCorrectId", "1234", ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of("id", "id", "abcd", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "id", "123", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "id", "12345", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "id", "1234", null)
        );
    }

    @ParameterizedTest(name = "Test set pincode. Given existing id: {0}, provided id: {1}, pincode: {2}. Should throw: {3}")
    @MethodSource("getSetPincodeTestScenarios")
    public void setPincodeTest(String existingId, String providedId, String pincode, String exceptionMessage) {
        User user = providedId.equals(existingId) ? User.builder().id(existingId).build() : null;
        expect(entityManager.find(User.class, providedId)).andReturn(user);
        replay(entityManager);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> userService.setPincode(providedId, pincode));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            userService.setPincode(providedId, pincode);
            assertNotNull(user);
            assertTrue(BCrypt.verifyer().verify(pincode.toCharArray(), user.getPincode()).verified);
        }
    }

    public Stream<Arguments> getVerifyPincodeTestScenarios() {
        return Stream.of(
                Arguments.of(null, "id", "1234", "5678", ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of(null, "id", "1234", "123", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of(null, "id", "1234", "abcd", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of(null, "id", "1234", "1234", ErrorMessages.USER_DOESNT_EXIST.name()),

                Arguments.of("id", "inCorrectId", "1234", "5678", ErrorMessages.USER_DOESNT_EXIST.name()),
                Arguments.of("id", "inCorrectId", "1234", "123", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "inCorrectId", "1234", "abcd", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "inCorrectId", "1234", "1234", ErrorMessages.USER_DOESNT_EXIST.name()),

                Arguments.of("id", "id", "123", "123", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "id", "abcd", "abcd", ErrorMessages.INCORRECT_PINCODE.name()),
                Arguments.of("id", "id", "12345", "12345", ErrorMessages.INCORRECT_PINCODE.name()),

                Arguments.of("id", "id", "1234", "1234", null)
        );
    }

    @ParameterizedTest(name = "Test verify pincode. Given existing id: {0}, provided id: {1}, real pincode: {2}," +
            " given pincode: {3}. Should throw: {4}")
    @MethodSource("getVerifyPincodeTestScenarios")
    public void verifyPinCodeTest(String existingId, String providedId, String realPincode, String givenPincode,
                                  String exceptionMessage
    ) {
        User user = providedId.equals(existingId) ? User.builder().id(existingId).build() : null;
        boolean isPincodeCorrect = realPincode.equals(givenPincode);
        expect(entityManager.find(User.class, providedId)).andReturn(user);
        replay(entityManager);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> userService.setPincode(providedId, givenPincode));
            assertEquals(exceptionMessage, thrown.getMessage());
        } else {
            userService.setPincode(providedId, givenPincode);
            assertNotNull(user);
            assertEquals(isPincodeCorrect, BCrypt.verifyer().verify(realPincode.toCharArray(), user.getPincode()).verified);
        }
    }

    @Test
    public void getAllTest() {
        // doesnt require a test
    }
}
