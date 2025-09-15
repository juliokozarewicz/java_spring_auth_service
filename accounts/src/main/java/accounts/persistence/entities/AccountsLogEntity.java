package accounts.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "user_log",
    uniqueConstraints = @UniqueConstraint(columnNames = "id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsLogEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(length = 256, nullable = false)
    private String ipAddress;

    @Column(updatable = false, nullable = false)
    private UUID idUser;

    @Column(length = 512, nullable = false)
    private String agent;

    @Column(length = 256, nullable = false)
    private String updateType;

    @Column(nullable = false)
    private String oldValue;

    @Column(nullable = false)
    private String newValue;

}
