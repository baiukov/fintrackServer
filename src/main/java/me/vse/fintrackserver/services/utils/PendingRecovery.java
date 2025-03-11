package me.vse.fintrackserver.services.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.vse.fintrackserver.model.User;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
public class PendingRecovery {


    private User user;

    @EqualsAndHashCode.Exclude
    private String code;

    @EqualsAndHashCode.Exclude
    private LocalDateTime timestamp;

    @EqualsAndHashCode.Exclude
    private boolean isVerified = false;

    public PendingRecovery(User user, String code) {
        this.user = user;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

}
