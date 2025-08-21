package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsVerificationTokenEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsVerificationTokenRepository extends

    JpaRepository<AccountsVerificationTokenEntity, UUID>

{

    // Get token by userEmail
    List<AccountsVerificationTokenEntity> findByEmail(String email);

    // Get token by userEmail and token
    Optional<AccountsVerificationTokenEntity> findByEmailAndToken(
        String email, String token
    );

}
