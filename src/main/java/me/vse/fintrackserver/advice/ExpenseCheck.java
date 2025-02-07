package me.vse.fintrackserver.advice;

import me.vse.fintrackserver.enums.AdviceMessages;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.repositories.UserRepository;
import me.vse.fintrackserver.rest.responses.AdviceResponse;
import me.vse.fintrackserver.services.AdviceService;
import me.vse.fintrackserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ExpenseCheck extends Advice {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdviceService adviceService;
    @Autowired
    private TransactionService transactionService;

    @Override
    public void perform() {
        int batchSize = 20;
        int pageNumber = 0;
        PageRequest request = PageRequest.of(pageNumber, batchSize);
        Page<User> users = userRepository.findAll(request);
        int totalPages = users.getTotalPages();

        for (int i = 0; i < totalPages; i++) {
            List<Account> accounts = users.stream()
                    .map(User::getAccountUserRights)
                    .flatMap(List::stream)
                    .map(AccountUserRights::getAccount)
                    .toList();
            for (Account account : accounts) {
                LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
                boolean isJanuary = lastMonth.getMonth().equals(Month.JANUARY);

                LocalDateTime startOfLastMonth = LocalDateTime.of(
                        isJanuary ? lastMonth.getYear() : lastMonth.getYear() - 1,
                        lastMonth.getMonth().minus(1),
                        1,
                        0,
                        0,
                        0
                );

                LocalDateTime startOfCurrentMonth = LocalDateTime.of(
                        LocalDate.now().getYear(),
                        LocalDate.now().getMonth(),
                        1,
                        0,
                        0,
                        0
                );

                double avgExpenseBeforeLastMonth = transactionService.getExpenseTransactions(account,
                                LocalDateTime.MIN,
                                startOfLastMonth)
                        .stream()
                        .mapToDouble(Transaction::getAmount)
                        .average()
                        .orElse(0.0);

                double avgExpenseLastMonth = transactionService.getExpenseTransactions(account,
                                startOfLastMonth,
                                startOfCurrentMonth)
                        .stream()
                        .mapToDouble(Transaction::getAmount)
                        .average()
                        .orElse(0.0);


                User owner = account.getUserRights().stream()
                        .filter(AccountUserRights::isOwner)
                        .map(AccountUserRights::getUser)
                        .findFirst()
                        .orElse(null);

                if (owner == null) continue;

                if (avgExpenseLastMonth > avgExpenseBeforeLastMonth) {
                    AdviceResponse adviceResponse = new AdviceResponse(
                            owner.getId(),
                            AdviceMessages.EXPENSES_ARE_HIGHER_THAN_USUAL,
                            List.of(Double.toString(avgExpenseLastMonth - avgExpenseBeforeLastMonth)));
                    adviceService.addResponse(adviceResponse);
                }

                request = PageRequest.of(i, batchSize);
                users = userRepository.findAll(request);
            }
        }
    }
}
