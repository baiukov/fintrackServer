package me.vse.fintrackserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="Users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    @ToString.Exclude
    @JsonIgnore
    private List<AccountUserRights> accountUserRights;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @ToString.Exclude
    @JsonIgnore
    private List<UserGroupRelation> userGroupRelations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @ToString.Exclude
    @JsonIgnore
    private List<Category> categories;

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String userName;

    @Column(name = "password")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private String password;

    @Column(name = "pincode")
    @JsonIgnore
    private String pincode;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @Column(name = "is_admin")
    private boolean isAdmin;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return isBlocked == user.isBlocked &&
                isAdmin == user.isAdmin &&
                Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(pincode, user.pincode) &&
                Objects.equals(
                        createdAt != null ? createdAt.truncatedTo(ChronoUnit.SECONDS) : null,
                        user.createdAt != null ? user.createdAt.truncatedTo(ChronoUnit.SECONDS) : null) &&
                Objects.equals(
                        updatedAt != null ? updatedAt.truncatedTo(ChronoUnit.SECONDS) : null,
                        user.updatedAt != null ? user.updatedAt.truncatedTo(ChronoUnit.SECONDS) : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                email,
                userName,
                pincode,
                isBlocked,
                isAdmin,
                createdAt != null ? createdAt.truncatedTo(ChronoUnit.SECONDS) : null,
                updatedAt != null ? updatedAt.truncatedTo(ChronoUnit.SECONDS) : null);
    }

}
