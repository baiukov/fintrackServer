package me.vse.fintrackserver.model;

import jakarta.persistence.*;
import lombok.*;
import me.vse.fintrackserver.enums.Frequencies;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "standing_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class StandingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "frequency")
    private Frequencies frequency;

    @OneToOne
    @JoinColumn(name = "transaction_sample_id", referencedColumnName = "id", nullable = false)
    private Transaction transactionSample;

    @Column(name = "last_repeated_at")
    @CreationTimestamp
    private LocalDateTime lastRepeatedAt;

    @Column(name = "remind_days_before")
    private Integer remindDaysBefore;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
