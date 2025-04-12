package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.AccountsVerificationTokenEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends

    JpaRepository<AccountsVerificationTokenEntity, UUID>

{

    // Get token by email
    List<AccountsVerificationTokenEntity> findByEmail(String email);

    // Get token by email and token
    Optional<AccountsVerificationTokenEntity> findByEmailAndToken(
        String email, String token
    );

}
