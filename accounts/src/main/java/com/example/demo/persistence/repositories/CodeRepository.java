package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.CodeEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CodeRepository extends

    JpaRepository<CodeEntity, UUID>

{

    // get token by code and email
    Optional<CodeEntity> findByCodeAndEmail(String code, String email);

}
