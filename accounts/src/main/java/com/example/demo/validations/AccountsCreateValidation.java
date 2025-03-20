package com.example.demo.validations;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsCreateValidation (

        @NotEmpty(message = "{is_required}")
        @Size(max = 255, message = "{many_characters}")
        @Pattern(
                regexp = "^[^<>&'\"/]+$",
                message = "{contains_disallowed_characters}"
        )
        String name,

        @NotEmpty(message = "{is_required}")
        @Size(max = 255, message = "{many_characters}")
        @Email(message = "{must_be_a_valid_email}")
        String email,

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
        String password,

        @NotEmpty(message = "{is_required}")
        @Pattern(
            regexp = "^(https?|ftp)://[^\s/$.?#].[^\s]*$",
            message = "{must_be_a_valid_link}"
        )
        String link

) {}
