package me.vse.fintrackserver.services;

import jakarta.transaction.Transactional;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.TransactionAggregation;
import me.vse.fintrackserver.model.dto.AccountAggregationDTO;
import me.vse.fintrackserver.repositories.AggregationRepository;
import me.vse.fintrackserver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionAggregationService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AggregationRepository aggregationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void aggregateAndSaveDailyTransactions() {
        LocalDateTime aDayAgo = LocalDateTime.now().minusDays(1);

        List<Object[]> results = transactionRepository.aggregateTransactionsForXDays(aDayAgo);

        for (Object[] row : results) {
            Account account = (Account) row[0];
            Category category = (Category) row[1];
            Double totalIncome = (Double) row[2];
            Double totalExpense = (Double) row[3];

            Optional<TransactionAggregation> existingAggregation = aggregationRepository
                    .findByAccountAndCategory(account, category);

            TransactionAggregation aggregation;
            if (existingAggregation.isPresent()) {
                aggregation = existingAggregation.get();
                Double prevIncome = aggregation.getTotalIncome();
                Double prevExpense = aggregation.getTotalExpense();

                aggregation.setTotalIncome(prevIncome + totalIncome);
                aggregation.setTotalExpense(prevExpense + totalExpense);
            } else {
                Object[] result = transactionRepository
                        .getAggregatedTransactionsForAccountAndCategory(account, category);

                totalIncome = (Double) result[0];
                totalExpense = (Double) result[1];

                aggregation = TransactionAggregation.builder()
                        .account(account)
                        .category(category)
                        .totalIncome(totalIncome)
                        .totalExpense(totalExpense)
                        .build();
            }
            aggregationRepository.save(aggregation);
        }
    }

    public Double getIncome(String accountId) {
        AccountAggregationDTO aggregation = aggregationRepository.getTotalIncomeAndExpenseByAccount(accountId);
        return aggregation != null ? aggregation.getTotalIncome() : null;
    }

    public Double getExpense(String accountId) {
        AccountAggregationDTO aggregation = aggregationRepository.getTotalIncomeAndExpenseByAccount(accountId);
        return aggregation != null ? aggregation.getTotalExpense() : null;
    }

    public Double getTotal(String accountId) {
        AccountAggregationDTO aggregation = aggregationRepository.getTotalIncomeAndExpenseByAccount(accountId);
        if (aggregation == null) {
            return null;
        }
        return aggregation.getTotalIncome() - aggregation.getTotalExpense();
    }

}
