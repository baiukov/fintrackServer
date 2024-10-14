package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.StandingOrder;
import me.vse.fintrackserver.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingOrderRepository extends JpaRepository<StandingOrder, String> {

}
