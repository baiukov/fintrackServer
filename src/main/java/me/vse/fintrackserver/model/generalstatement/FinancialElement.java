package me.vse.fintrackserver.model.generalstatement;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class FinancialElement {

    private String name;

    private List<FinancialElementRow> transactions;

    private String totalDebit = "0";
    private String totalCredit = "0";

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return this.name.equals(((FinancialElement) other).name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

}
