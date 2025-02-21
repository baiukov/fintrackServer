package me.vse.fintrackserver.advice;

import jakarta.transaction.Transactional;
import lombok.*;
import me.vse.fintrackserver.enums.AdviceMessages;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.repositories.UserRepository;
import me.vse.fintrackserver.rest.responses.AdviceResponse;
import me.vse.fintrackserver.rest.responses.TransactionByCategoryResponse;
import me.vse.fintrackserver.services.AdviceService;
import me.vse.fintrackserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExpenseByCategoryCheck extends Advice {


    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AdviceService adviceService;

    private Frequencies frequency = Frequencies.MONTHLY;
    private int dayOfExecution = 1;

    @Override
    @Transactional
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

                Map<Category, Double> categoryAvgExpenseTheMonthBeforeLast =
                        getAverageCategoriesExpenses(transactionService.findAllByCategories(
                                account.getId(),
                                null,
                                startOfLastMonth,
                                false)
                        );

                Map<Category, Double> categoryAvgExpenseLastMonth =
                        getAverageCategoriesExpenses(transactionService.findAllByCategories(
                                account.getId(),
                                startOfLastMonth,
                                null,
                                false)
                        );

                User owner = account.getUserRights().stream()
                        .filter(AccountUserRights::isOwner)
                        .map(AccountUserRights::getUser)
                        .findFirst()
                        .orElse(null);

                if (owner == null) continue;

                AtomicReference<AdviceResponse> advice = new AtomicReference<>();
                categoryAvgExpenseLastMonth.forEach((category, avgExpense) -> {
                    if (advice.get() != null) return;
                    Double lastAvgExpense = categoryAvgExpenseTheMonthBeforeLast.get(category);
                    if (lastAvgExpense == null) return;
                    if (avgExpense > lastAvgExpense) {
                        double difference = Math.floor(avgExpense - lastAvgExpense);
                        advice.set(AdviceResponse.builder()
                                        .userId(owner.getId())
                                        .message(AdviceMessages.EXPENSES_ARE_HIGHER_THAN_USUAL)
                                        .arguments(List.of(category.getName(), Double.toString(difference)))
                                        .build());
                    }
                });

                if (advice.get() != null) {
                    adviceService.addResponse(advice.get());
                }

                request = PageRequest.of(i, batchSize);
                users = userRepository.findAll(request);
            }
        }
    }

    private Map<Category, Double> getAverageCategoriesExpenses(List<TransactionByCategoryResponse> expenses) {
        return expenses.stream()
                .collect(Collectors.toMap(
                        TransactionByCategoryResponse::getCategory,
                        entry -> entry.getTransactions()
                                .stream()
                                .mapToDouble(Transaction::getAmount)
                                .average()
                                .orElse(0.0)
                ));
    }

}
