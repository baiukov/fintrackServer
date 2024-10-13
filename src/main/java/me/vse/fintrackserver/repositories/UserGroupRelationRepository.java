package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.UserGroupRelation;
import me.vse.fintrackserver.model.identifiers.UserGroupRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRelationRepository extends JpaRepository<UserGroupRelation, UserGroupRelationId> {
}
