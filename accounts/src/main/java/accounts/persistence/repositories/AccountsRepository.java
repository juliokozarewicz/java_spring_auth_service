package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountsRepository extends

    JpaRepository<AccountsEntity, String>

{

    // Get user by userEmail
    Optional<AccountsEntity> findByEmail(String email);

    // Get user by ID and userEmail
    Optional<AccountsEntity> findByIdAndEmail(String id, String email);

}
