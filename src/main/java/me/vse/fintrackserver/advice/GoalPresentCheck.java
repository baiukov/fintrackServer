package me.vse.fintrackserver.advice;

import lombok.AllArgsConstructor;
import me.vse.fintrackserver.enums.AdviceMessages;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.Transaction;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.repositories.UserRepository;
import me.vse.fintrackserver.rest.responses.AdviceResponse;
import me.vse.fintrackserver.services.AccountService;
import me.vse.fintrackserver.services.AdviceService;
import me.vse.fintrackserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class GoalPresentCheck extends Advice {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final AdviceService adviceService;

    public void perform() {
        int batchSize = 20;
        int pageNumber = 0;
        PageRequest request = PageRequest.of(pageNumber, batchSize);
        Page<User> users = userRepository.findAll(request);
        int totalPages = users.getTotalPages();

        for (int i = 0; i < totalPages; i++) {
            for (User user : users) {

                List<Account> accounts = users.stream()
                        .map(User::getAccountUserRights)
                        .flatMap(List::stream)
                        .map(AccountUserRights::getAccount)
                        .toList();

                Account goalAccount = accounts.stream()
                        .filter(account -> account.getGoalAmount() > 0)
                        .findFirst()
                        .orElse(null);

                if (goalAccount == null) {
                    adviceService.addResponse(new AdviceResponse(
                            user.getId(),
                            AdviceMessages.RESERVE_FUND_CREATION,
                            List.of()
                    ));
                }
            }
        }
    }
}
