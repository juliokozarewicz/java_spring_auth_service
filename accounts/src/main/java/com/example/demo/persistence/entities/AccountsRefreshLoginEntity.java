package com.example.demo.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_refresh_login", uniqueConstraints = @UniqueConstraint(columnNames = "id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsRefreshLoginEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 256, nullable = false)
    private String email;

    @Column(length = 1024, nullable = false)
    private String token;

    @Column(length = 256, nullable = false)
    private String ipAddress;

    @Column(length = 512, nullable = false)
    private String agent;

}
