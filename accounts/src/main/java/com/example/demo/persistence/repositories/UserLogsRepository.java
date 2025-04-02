package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.UserLogsEntity;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLogsRepository extends

    JpaRepository<UserLogsEntity, UUID>

{ }
