package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.AccountsUserLogEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogsRepository extends

    JpaRepository<AccountsUserLogEntity, UUID>

{ }
