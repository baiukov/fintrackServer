package me.vse.fintrackserver.services;

import me.vse.fintrackserver.ATest;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.repositories.UserRepository;
import org.easymock.EasyMock;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.stream.Stream;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest extends ATest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    protected void setupMocks() {
        userRepository = EasyMock.createMock(UserRepository.class);
    }

    private Stream<Arguments> getPasswordTestCases() {
        return Stream.of(
                Arguments.of("1234567", ErrorMessages.PASSWORD_LESS_THAN_8_CHARS, false)
        );
    }

    @ParameterizedTest(name = "Given password {0} should be failed: {2} due to: {1}")
    @MethodSource("getPasswordTestCases")
    public void testValidatePassword(String password, ErrorMessages error, boolean isValid) {
        expect(userRepository.findByUserName(anyString())).andReturn(null).anyTimes();
        expect(userRepository.findByEmail(anyString())).andReturn(null).anyTimes();
        replay(userRepository);

        if (isValid) {
            assertNotNull(userService.registerUser(anyString(), anyString(), password)) ;
        } else {
            assertThrows(IllegalArgumentException.class, () -> userService.registerUser(anyString(), anyString(), password));
        }

        verify(userService);
    }


}
