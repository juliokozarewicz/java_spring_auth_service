package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsAddressRepository extends

    JpaRepository<AccountsAddressEntity, String>

{

    // Get address by user id
    List<AccountsAddressEntity> findByUserId(String id);

}
