package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsUserLogEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogsRepository extends

    JpaRepository<AccountsUserLogEntity, UUID>

{ }
