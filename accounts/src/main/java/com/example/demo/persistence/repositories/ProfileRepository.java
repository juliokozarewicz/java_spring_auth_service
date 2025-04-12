package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.AccountsProfileEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProfileRepository extends

    JpaRepository<AccountsProfileEntity, UUID>

{

    // Get profile by id
    Optional<AccountsProfileEntity> findById(String id);

}
