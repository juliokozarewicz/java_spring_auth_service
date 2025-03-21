package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.ProfileEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProfileRepository extends

    JpaRepository<ProfileEntity, UUID>

{

    // get profile by id
    Optional<ProfileEntity> findById(String id);

}
