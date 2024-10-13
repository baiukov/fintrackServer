package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {

    @Query("select g from Group g where g.code = :code")
    Group findByCode(@Param("code") String groupCode);
}
