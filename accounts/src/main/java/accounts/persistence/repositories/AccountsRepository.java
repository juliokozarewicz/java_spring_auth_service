package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountsRepository extends

    JpaRepository<AccountsEntity, String>

{

    // Get user by email
    Optional<AccountsEntity> findByEmail(String email);

}
