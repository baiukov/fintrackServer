package me.vse.fintrackserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.enums.AccountType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "account")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, updatable = false)
    private String id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @ToString.Exclude
    @JsonIgnore
    private List<AccountUserRights> userRights;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @ToString.Exclude
    @JsonIgnore
    private List<Asset> assets;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @ToString.Exclude
    @JsonIgnore
    private List<Transaction> transactions;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private AccountType type;

    @Column(name = "currency")
    private Currency currency;

    @Column(name = "initial_amount")
    private double initialAmount;

    @Column(name = "interest_rate")
    private double interestRate;

    @Column(name = "goal_amount")
    private Double goalAmount;

    @Column(name = "already_paid_amount")
    private Double alreadyPaidAmount;

    @Column(name = "icon")
    private String emoji;

    @Column(name = "is_removed", insertable = false)
    private boolean isRemoved;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;

        return Double.compare(account.initialAmount, initialAmount) == 0 &&
                Double.compare(account.interestRate, interestRate) == 0 &&
                isRemoved == account.isRemoved &&
                Objects.equals(id, account.id) &&
                Objects.equals(name, account.name) &&
                type == account.type &&
                Objects.equals(currency, account.currency) &&
                Objects.equals(goalAmount, account.goalAmount) &&
                Objects.equals(alreadyPaidAmount, account.alreadyPaidAmount) &&
                Objects.equals(
                        createdAt != null ? createdAt.truncatedTo(ChronoUnit.SECONDS) : null,
                        account.createdAt != null ? account.createdAt.truncatedTo(ChronoUnit.SECONDS) : null) &&
                Objects.equals(
                        updatedAt != null ? updatedAt.truncatedTo(ChronoUnit.SECONDS) : null,
                        account.updatedAt != null ? account.updatedAt.truncatedTo(ChronoUnit.SECONDS) : null) &&
                Objects.equals(
                        removedAt != null ? removedAt.truncatedTo(ChronoUnit.SECONDS) : null,
                        account.removedAt != null ? account.removedAt.truncatedTo(ChronoUnit.SECONDS) : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                type,
                currency,
                initialAmount,
                interestRate,
                goalAmount,
                alreadyPaidAmount,
                isRemoved,
                createdAt != null ? createdAt.truncatedTo(ChronoUnit.SECONDS) : null,
                updatedAt != null ? updatedAt.truncatedTo(ChronoUnit.SECONDS) : null,
                removedAt != null ? removedAt.truncatedTo(ChronoUnit.SECONDS) : null);
    }

}
