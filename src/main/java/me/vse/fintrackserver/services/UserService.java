package me.vse.fintrackserver.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public User registerUser(String email, String userName, String password) {
        if (userRepository.findByUserName(userName) != null) {
            throw new IllegalArgumentException(ErrorMessages.USERNAME_EXISTS.name());
        }

        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException(ErrorMessages.EMAIL_EXISTS.name());
        }

        ErrorMessages validatePasswordError = validatePassword(password);
        if (validatePasswordError != null) {
            throw new IllegalArgumentException(validatePasswordError.name());
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        User user = User.builder()
                .userName(userName)
                .email(email)
                .password(hashedPassword)
                .build();

        entityManager.persist(user);
        return user;
    }

    private ErrorMessages validatePassword(String password) {
        String moreThan8Character = "^.{8,}$";
        String containsUpperCaseLetter = "^(?=.*[A-Z]).+$";
        String containsLowerCaseLetter = "^(?=.*[a-z]).+$";
        String containsDigit = "^(?=.*[0-9]).+$";
        String doesntContainWhiteSpace = "^(?!.*\\s).+$";
        if (!password.matches(moreThan8Character)) {
            return ErrorMessages.PASSWORD_LESS_THAN_8_CHARS;
        } else if (!password.matches(containsUpperCaseLetter)) {
            return ErrorMessages.PASSWORD_DOESNT_CONTAIN_UPPERCASE_LETTER;
        } else if (!password.matches(containsLowerCaseLetter)) {
            return ErrorMessages.PASSWORD_DOESNT_CONTAIN_LOWERCASE_LETTER;
        } else if (!password.matches(containsDigit)) {
            return ErrorMessages.PASSWORD_DOESNT_CONTAIN_DIGIT;
        } else if (!password.matches(doesntContainWhiteSpace)) {
            return ErrorMessages.PASSWORD_CONTAINS_WHITESPACES;
        } else {
            return null;
        }
    }

    public User login(String email, String userName, String password) {

        User user = userRepository.findByUserNameOrEmail(userName, email);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        if (!BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified) {
            throw new IllegalArgumentException(ErrorMessages.WRONG_PASSWORD.name());
        }

        return user;
    }

    public void setPincode(UUID id, String pincode) {
        String only4DigitsRegex = "^\\d{4}$";
        if (!pincode.matches(only4DigitsRegex)) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_PINCODE.name());
        }

        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        String hasedPincode = BCrypt.withDefaults().hashToString(12, pincode.toCharArray());
        user.setPincode(hasedPincode);
    }

    public boolean verifyPinCode(UUID id, String pincode) {
        String only4DigitsRegex = "^\\d{4}$";
        if (!pincode.matches(only4DigitsRegex)) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_PINCODE.name());
        }

        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        return BCrypt.verifyer().verify(pincode.toCharArray(), user.getPincode()).verified;
    }


}
