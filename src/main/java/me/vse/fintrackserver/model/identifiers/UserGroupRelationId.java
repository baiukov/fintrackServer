package me.vse.fintrackserver.model.identifiers;

import lombok.*;
import me.vse.fintrackserver.model.Group;
import me.vse.fintrackserver.model.User;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupRelationId implements Serializable {

    private User user;
    private Group group;

}
