package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.model.identifiers.UserGroupRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserGroupRelationRepository extends JpaRepository<UserGroupRelation, UserGroupRelationId> {

    @Modifying
    @Transactional
    @Query("delete from UserGroupRelation ugr where ugr.group.id = :groupId and ugr.user.id = :userId")
    void deleteByGroupAndUser(@Param("groupId") String groupId, @Param("userId") String userId);

}
