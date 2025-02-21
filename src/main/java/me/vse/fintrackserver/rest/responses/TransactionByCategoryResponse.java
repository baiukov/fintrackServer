package me.vse.fintrackserver.rest.responses;

import lombok.*;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.Transaction;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TransactionByCategoryResponse {

    private Category category;
    private List<Transaction> transactions;

}
