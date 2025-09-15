package accounts.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "user_address",
    uniqueConstraints = @UniqueConstraint(columnNames = "id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsAddressEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(length = 256, nullable = false)
    private String addressName;

    @Column(length = 50, nullable = false)
    private String zipCode;

    @Column(length = 256, nullable = false)
    private String street;

    @Column(length = 50, nullable = false)
    private String number;

    @Column(length = 256, nullable = true)
    private String addressLineTwo;

    @Column(length = 256, nullable = false)
    private String neighborhood;

    @Column(length = 256, nullable = false)
    private String city;

    @Column(length = 256, nullable = false)
    private String state;

    @Column(length = 256, nullable = false)
    private String country;

    @Column(length = 256, nullable = false)
    private String addressType;

    @Column(nullable = false)
    private Boolean isPrimary;

    @Column(length = 256, nullable = true)
    private String landmark;

    @Column(updatable = false, nullable = false)
    private UUID idUser;
}
