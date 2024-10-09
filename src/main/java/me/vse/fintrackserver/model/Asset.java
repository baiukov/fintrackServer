package me.vse.fintrackserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, updatable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ToString.Exclude
    private Account account;

    @Column(name = "name")
    private String name;

    @Column(name = "acquisition_price")
    private Long acquisitionPrice;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "color")
    private String color;

    @Column(name = "icon")
    private String icon;

    @Column(name = "is_removed")
    private boolean isRemoved;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
