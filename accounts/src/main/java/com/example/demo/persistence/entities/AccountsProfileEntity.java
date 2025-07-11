package com.example.demo.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_profile",
    uniqueConstraints = @UniqueConstraint(columnNames = "id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsProfileEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 555, nullable = true)
    private String profileImage;

    @Column(length = 256, nullable = true)
    private String name;

    @Column(length = 25, nullable = true)
    private String phone;

    @Column(length = 256, nullable = true)
    private String identityDocument;

    @Column(length = 256, nullable = true)
    private String gender;

    @Column(length = 50, nullable = true)
    private String birthdate;

    @Column(length = 256, nullable = true)
    private String biography;

    @Column(length = 50, nullable = true)
    private String language;

}
