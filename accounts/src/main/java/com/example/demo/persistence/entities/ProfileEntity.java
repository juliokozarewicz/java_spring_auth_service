package com.example.demo.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile", uniqueConstraints = @UniqueConstraint(columnNames = "user"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProfileEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 255, nullable = true)
    private String name;

    @Column(length = 25, nullable = true)
    private String phone;

    @Column(length = 255, nullable = true)
    private String identityDocument;

    @Column(length = 255, nullable = true)
    private String gender;

    @Column(length = 555, nullable = true)
    private String profileImage;

    @Column(length = 255, nullable = false)
    private String userId;

}
