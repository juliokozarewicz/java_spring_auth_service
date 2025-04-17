package com.example.demo.persistence.repositories;

import com.example.demo.persistence.entities.AccountsRefreshLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshLoginRepository extends

    JpaRepository<AccountsRefreshLoginEntity, String>

{

    // Get token by token
    List<AccountsRefreshLoginEntity> findByToken(String token);

    // Get token by email
    List<AccountsRefreshLoginEntity> findByEmail(String email);

}
