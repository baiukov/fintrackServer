package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Transaction;
import me.vse.fintrackserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("select t from Transaction t where t.account = :account or t.receiver = :account")
    List<Transaction> findAllByAccount(@Param("account") Account account);

    @Query("select t from Transaction t where (t.account = :account or t.receiver = :account) " +
            "and t.executionDateTime >= :fromDate and t.executionDateTime <= :endDate")
    List<Transaction> findAllByAccount(@Param("account") Account account,
                                       @Param("fromDate") LocalDateTime fromDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("select t from Transaction t where (t.account = :account or t.receiver = :account) " +
            "and t.executionDateTime <= :endDate")
    List<Transaction> findAllByAccount(@Param("account") Account account,
                                       @Param("endDate") LocalDateTime endDate);

}
