package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.TransactionAggregation;
import me.vse.fintrackserver.model.dto.AccountAggregationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AggregationRepository extends JpaRepository<TransactionAggregation, String> {

    Optional<TransactionAggregation> findByAccountId(String accountId);

    Optional<TransactionAggregation> findByAccountAndCategory(Account account, Category category);

    @Query("""
        SELECT new me.vse.fintrackserver.model.dto.AccountAggregationDTO(
            t.account.id,
            SUM(t.totalIncome),
            SUM(t.totalExpense)
        )
        FROM TransactionAggregation t
        WHERE t.account.id = :accountId
        GROUP BY t.account.id
    """)
    AccountAggregationDTO getTotalIncomeAndExpenseByAccount(@Param("accountId") String accountId);

}
