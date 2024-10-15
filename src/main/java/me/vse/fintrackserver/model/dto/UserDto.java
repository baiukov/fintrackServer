package me.vse.fintrackserver.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.UserGroupRelation;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDto {

    private String id;
    private String email;
    private String userName;
    private boolean isBlocked;
    private boolean isAdmin;
}
