package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("select u.id from User u where lower(u.userName) = lower(:username)")
    String findByUserName(@Param("username") String username);

    @Query("select u.id from User u where lower(u.email) = lower(:email)")
    String findByEmail(@Param("email") String email);

    @EntityGraph(value = "user.basic", type = EntityGraph.EntityGraphType.LOAD)
    @Query("select u from User u where lower(u.userName) = lower(:username) or lower(u.email) = lower(:email)")
    User findByUserNameOrEmail(@Param("username") String username, @Param("email") String email);
}
