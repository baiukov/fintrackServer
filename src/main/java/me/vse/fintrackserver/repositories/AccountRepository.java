package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {


}
