package me.vse.fintrackserver.model.generalstatement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FinancialElementRow {

    private String debit;
    private String credit;

}
