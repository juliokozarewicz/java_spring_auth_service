package com.example.demo.validations;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountsCreateValidation {

        @NotEmpty(message = "{is_required}")
        @Size(min = 1, max = 255, message = "{many_characters}")
        @Pattern(
                regexp = "^[^<>&'\"/]+$",
                message = "{contains_disallowed_characters}"
        )
        private String name;

        @NotEmpty(message = "{is_required}")
        @Email(message = "{must_be_a_valid_email}")
        @Size(max = 255, message = "{many_characters}")
        private String email;

        @NotEmpty(message = "{is_required}")
        @Size(
                min = 8, max = 255,
                message = "{must_be_at_least_8_characters_long}"
        )
        @Pattern(
                regexp = "^[^<>&'\"/]+$",
                message = "{contains_disallowed_characters}"
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
                message = "{must_contain_at_least_one_uppercase_letter}"
        )
        private String password;

}
