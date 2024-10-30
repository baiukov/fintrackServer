package me.vse.fintrackserver.rest.responses;

import lombok.*;
import me.vse.fintrackserver.enums.AdviceMessages;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdviceResponse {

    private String userId;
    private AdviceMessages message;
    private List<String> arguments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdviceResponse that = (AdviceResponse) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
