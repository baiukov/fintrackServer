package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("""
        SELECT a FROM Account a
        JOIN AccountUserRights aur ON a.id = aur.account.id
        JOIN User u ON u.id = aur.user.id
        WHERE u.id = :id
        AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Account> findByUserIdAndName(@Param("id") String id, @Param("name") String name, Pageable pageable);

    @Query("select a from Account a where lower(a.id) in (:ids)")
    List<Account> findAllByIds(@Param("ids") List<String> ids);
}
