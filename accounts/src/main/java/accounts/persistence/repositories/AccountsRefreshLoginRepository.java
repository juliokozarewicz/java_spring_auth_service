package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsRefreshLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsRefreshLoginRepository extends

    JpaRepository<AccountsRefreshLoginEntity, String>

{

    // Get token by token
    Optional<AccountsRefreshLoginEntity> findByToken(String token);

    // Delete token by token
    Optional<AccountsRefreshLoginEntity> deleteByToken(String token);

    // Get token by email
    List<AccountsRefreshLoginEntity> findByEmail(String email);

}
