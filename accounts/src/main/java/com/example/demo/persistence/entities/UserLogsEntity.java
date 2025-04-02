package com.example.demo.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_logs",
    uniqueConstraints = @UniqueConstraint(columnNames = "id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserLogsEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 256, nullable = false)
    private String ipAddress;

    @Column(updatable = false, nullable = false)
    private String userId;

    @Column(length = 512, nullable = false)
    private String agent;

    @Column(length = 256, nullable = false)
    private String updateType;

    @Column(nullable = false)
    private String oldValue;

    @Column(nullable = false)
    private String newValue;

}
