package me.vse.fintrackserver.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.Messages;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.SimplifiedEntityDto;
import me.vse.fintrackserver.repositories.CategoryRepository;
import me.vse.fintrackserver.repositories.UserRepository;
import me.vse.fintrackserver.services.utils.PendingRecovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.time.LocalDateTime;
import java.util.*;


@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Transactional
    public List<User> getAll(int pageSize, int pageNumber) {
        return userRepository.findAllPageable(PageRequest.of(pageNumber, pageSize));
    }

    @Transactional
    public List<SimplifiedEntityDto> getByName(String name, int limit) {
        List<User> users = userRepository.findAllByUserName(name, PageRequest.of(0, limit));
        List<SimplifiedEntityDto> simplifiedUsers = new ArrayList<>();
        for (User user : users) {
            simplifiedUsers.add(
                    new SimplifiedEntityDto(user.getId(), user.getUserName())
            );
        }
        return simplifiedUsers;
    }

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

        List<Category> categories = List.of(
                Category.builder().icon("🛒").name("Groceries").color("#FF5733").user(user).build(),
                Category.builder().icon("🍽️").name("Dining Out").color("#33FF57").user(user).build(),
                Category.builder().icon("🚗").name("Transportation").color("#3357FF").user(user).build(),
                Category.builder().icon("❤️").name("Health").color("#FF33A1").user(user).build(),
                Category.builder().icon("🎬").name("Entertainment").color("#FFBB33").user(user).build(),
                Category.builder().icon("💼").name("Work").color("#A133FF").user(user).build(),
                Category.builder().icon("🏠").name("Rent").color("#33FFF5").user(user).build(),
                Category.builder().icon("📚").name("Education").color("#F533FF").user(user).build(),
                Category.builder().icon("🎁").name("Gifts").color("#33FFA1").user(user).build(),
                Category.builder().icon("✈️").name("Travel").color("#FF5733").user(user).build()
        );

        categoryRepository.saveAll(categories);
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

    public void setPincode(String id, String pincode) {
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
        userRepository.save(user);
    }

    public boolean verifyPinCode(String id, String pincode) {
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

    private final Set<PendingRecovery> pendingRecoveries = new HashSet<>();
    private final int MAX_PENDING_RECOVERY_TIME_IN_MINUTES = 10;

    public void sendCode(String login, String language) throws IllegalArgumentException, MessagingException {

        User user = userRepository.findByUserNameOrEmail(login, login);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }
        pendingRecoveries.removeIf(pr -> pr.getUser().getId().equals(user.getId()));

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Messages messages = Messages.getInstance();

        String subject = messages.get(language, "PASSWORD_RECOVERY");

        Context context = new Context();
        context.setVariable("PASSWORD_RECOVERY", subject);
        context.setVariable("ENTER_CODE", messages.get(language, "ENTER_CODE"));
        context.setVariable("logoPath", "/img/logoSmall.svg");

        String code = Math.round(Math.random() * 899999 + 100000) + "";
        pendingRecoveries.add(new PendingRecovery(user, code));
        context.setVariable("code", code);

        String htmlContent = templateEngine.process("html/passwordRecovery", context);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        message.setFrom(new InternetAddress("noreply@fintrackserver.ru"));


        javaMailSender.send(message);
    }

    @Scheduled(fixedRate = 60 * 1000)
    protected void cleanPendingRecoveries() {
        LocalDateTime now = LocalDateTime.now();
        pendingRecoveries.removeIf(pendingRecovery -> pendingRecovery.getTimestamp().isBefore(
                now.minusMinutes(MAX_PENDING_RECOVERY_TIME_IN_MINUTES)
        ));
    }

    public boolean verifyCode(String login, String code) {
        User user = userRepository.findByUserNameOrEmail(login, login);
        if (user == null) {
            return false;
        }

        PendingRecovery pendingRecovery = pendingRecoveries.stream()
                .filter(pr -> pr.getUser().getId().equals(user.getId()) && pr.getCode().equals(code))
                .findFirst()
                .orElse(null);

        if (pendingRecovery == null) {
            return false;
        }

        pendingRecovery.setVerified(true);
        return true;
    }

    public void updatePassword(String login, String newPassword) throws IllegalArgumentException {
        User user = userRepository.findByUserNameOrEmail(login, login);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        PendingRecovery pendingRecovery = pendingRecoveries.stream()
                .filter(pr -> pr.getUser().getId().equals(user.getId()) && pr.isVerified())
                .findFirst()
                .orElse(null);

        if (pendingRecovery == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }
        pendingRecoveries.remove(pendingRecovery);

        ErrorMessages validatePasswordError = validatePassword(newPassword);
        if (validatePasswordError != null) {
            throw new IllegalArgumentException(validatePasswordError.name());
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
}
