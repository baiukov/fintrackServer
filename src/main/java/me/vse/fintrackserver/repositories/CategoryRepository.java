package me.vse.fintrackserver.repositories;

import jakarta.transaction.Transactional;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.StandingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    @Transactional
    @Modifying
    void deleteByUserId(String userId);

}
