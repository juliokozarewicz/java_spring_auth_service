package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountsLogRepository extends

    JpaRepository<AccountsLogEntity, UUID>

{ }
