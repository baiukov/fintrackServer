package me.vse.fintrackserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.enums.AccountType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@NamedEntityGraph(
        name = "account.plain",
        attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("name"),
                @NamedAttributeNode("type"),
                @NamedAttributeNode("currency"),
                @NamedAttributeNode("initialAmount"),
                @NamedAttributeNode("interestRate"),
                @NamedAttributeNode("goalAmount"),
                @NamedAttributeNode("alreadyPaidAmount"),
                @NamedAttributeNode("isRemoved")
        }
)
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

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private AccountType type;

    @Column(name = "currency")
    private Currency currency;

    @Column(name = "initial_amount")
    private Long initialAmount;

    @Column(name = "interest_rate")
    private double interestRate;

    @Column(name = "goal_amount")
    private Long goalAmount;

    @Column(name = "already_paid_amount")
    private Long alreadyPaidAmount;

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

}
