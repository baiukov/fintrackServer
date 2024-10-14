package me.vse.fintrackserver.model;

import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.model.identifiers.UserGroupRelationId;

@Entity
@Table(name = "user_group_relation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@IdClass(UserGroupRelationId.class)
public class UserGroupRelation {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    private Group group;

}
