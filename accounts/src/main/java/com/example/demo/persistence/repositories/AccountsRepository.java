package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.AccountsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.hibernate.validator.constraints.UUID;

@Repository
public interface AccountsRepository extends

    JpaRepository<AccountsEntity, UUID>,
    JpaSpecificationExecutor<AccountsEntity>

{

    // get user by id
    Optional<AccountsEntity> findById(String id);

}
