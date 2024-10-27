package me.vse.fintrackserver.model;

import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.enums.TransactionTypes;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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

    @Column(name = "is_removed")
    private boolean isRemoved;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (!Objects.equals(that.id, id)) return false;
        if (!Objects.equals(that.account, account)) return false;
        if (!Objects.equals(forAsset, that.forAsset)) return false;
        if (!Objects.equals(receiver, that.receiver)) return false;
        if (!Objects.equals(category, that.category)) return false;
        if (!Objects.equals(standingOrder, that.standingOrder)) return false;
        if (type != that.type) return false;
        if (Double.compare(that.amount, amount) != 0) return false;
        if (isRemoved != that.isRemoved) return false;
        if (!Objects.equals(lat, that.lat)) return false;
        if (!Objects.equals(lon, that.lon)) return false;
        if (!Objects.equals(photo, that.photo)) return false;
        if (!Objects.equals(createdAt, that.createdAt)) return false;
        if (!Objects.equals(updatedAt, that.updatedAt)) return false;

        if (executionDateTime != null && that.executionDateTime != null) {
            LocalDateTime thisExecutionWithoutSeconds = this.executionDateTime.withSecond(0).withNano(0);
            LocalDateTime thatExecutionWithoutSeconds = that.executionDateTime.withSecond(0).withNano(0);
            return thisExecutionWithoutSeconds.equals(thatExecutionWithoutSeconds);
        } else return executionDateTime == null && that.executionDateTime == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = id.hashCode();
        result = prime * result + account.hashCode();
        result = prime * result + (forAsset != null ? forAsset.hashCode() : 0);
        result = prime * result + (receiver != null ? receiver.hashCode() : 0);
        result = prime * result + (category != null ? category.hashCode() : 0);
        result = prime * result + (standingOrder != null ? standingOrder.hashCode() : 0);
        result = prime * result + type.hashCode();
        long temp = Double.doubleToLongBits(amount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (lat != null ? lat.hashCode() : 0);
        result = prime * result + (lon != null ? lon.hashCode() : 0);
        result = prime * result + (photo != null ? photo.hashCode() : 0);
        result = prime * result + (isRemoved ? 1 : 0);
        result = prime * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = prime * result + (updatedAt != null ? updatedAt.hashCode() : 0);

        result = prime * result + (executionDateTime != null ?
                executionDateTime.withSecond(0).withNano(0).hashCode() : 0);

        return result;
    }
}
