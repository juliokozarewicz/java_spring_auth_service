package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.String;
import java.util.Optional;

@Repository
public interface AccountsProfileRepository extends

    JpaRepository<AccountsProfileEntity, java.lang.String>

{

    // Get profile by id
    Optional<AccountsProfileEntity> findById(String id);

}
