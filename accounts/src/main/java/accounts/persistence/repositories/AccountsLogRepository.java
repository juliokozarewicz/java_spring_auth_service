package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsLogRepository extends

    JpaRepository<AccountsLogEntity, String>

{ }
