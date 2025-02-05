package me.vse.fintrackserver.model;

import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.enums.TransactionTypes;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "for_asset_id", referencedColumnName = "id")
    private Asset forAsset;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Account receiver;

    @ManyToOne
    @JoinColumn(name = "category", referencedColumnName = "id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "transactionSample")
    private StandingOrder standingOrder;

    @Column(name = "type")
    private TransactionTypes type;

    @Column(name = "amount")
    private double amount;

    @Column(name = "execution_date")
    private LocalDateTime executionDateTime;

    @Column(name = "note")
    private String note;

    @Column(name = "place_lat")
    private Double lat;

    @Column(name = "place_lon")
    private Double lon;

    @Column(name = "photo")
    private String photo;

    @Column(name = "icon")
    private String icon;

    @Column(name = "is_removed")
    private boolean isRemoved;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
