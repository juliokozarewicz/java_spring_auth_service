package accounts.persistence.repositories;

import accounts.persistence.entities.AccountsAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsAddressRepository extends

    JpaRepository<AccountsAddressEntity, String>

{

    // Get address by user id
    List<AccountsAddressEntity> findByUserId(String userId);

    // Get address by id and user id
    Optional<AccountsAddressEntity> findByIdAndUserId(
        String addressId, String userId
    );

}
