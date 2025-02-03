package me.vse.fintrackserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, updatable = false)
    private String id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    @ToString.Exclude
    @JsonIgnore
    private List<UserGroupRelation> groupUsersRelations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    @ToString.Exclude
    @JsonIgnore
    private List<AccountGroupRelation> accountGroupsRelations;

    @Column(name = "group_name")
    private String name;

    @Column(name = "group_code", unique = true)
    private String code;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
