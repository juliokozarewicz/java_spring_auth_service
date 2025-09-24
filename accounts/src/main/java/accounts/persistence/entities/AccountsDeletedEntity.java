package accounts.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "user_account_deleted"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsDeletedEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 256)
    private String email;

}
