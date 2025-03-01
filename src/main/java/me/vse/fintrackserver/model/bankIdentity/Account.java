package me.vse.fintrackserver.model.bankIdentity;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Account {

//    private String id;

    private String name;
    private String currency;

    private Double balance;
}
