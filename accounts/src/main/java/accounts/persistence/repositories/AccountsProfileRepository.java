package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountsProfileRepository extends

    JpaRepository<AccountsProfileEntity, UUID>

{

    // Get profile by id
    Optional<AccountsProfileEntity> findById(UUID idUser);

}