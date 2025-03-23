package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.VerificationTokenEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends

    JpaRepository<VerificationTokenEntity, UUID>

{

    // get token by email and token
    Optional<VerificationTokenEntity> findByEmailAndToken(
        String email, String token
    );

}
