package accounts.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "user_account",
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @Column(nullable = false, length = 256)
    private String level = "user";

    @Column(nullable = false, length = 256)
    private String email;

    @Column(nullable = false, length = 256)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean banned = false;

}
