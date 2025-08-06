package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsProfileEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountsProfileRepository extends

    JpaRepository<AccountsProfileEntity, UUID>

{

    // Get profile by id
    Optional<AccountsProfileEntity> findById(String id);

}
