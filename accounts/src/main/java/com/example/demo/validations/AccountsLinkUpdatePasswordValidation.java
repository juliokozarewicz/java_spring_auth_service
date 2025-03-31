package com.example.demo.validations;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsLinkUpdatePasswordValidation(

        @NotEmpty(message = "{is_required}")
        @Size(max = 255, message = "{many_characters}")
        @Email(message = "{must_be_a_valid_email}")
        String email,

        @NotEmpty(message = "{is_required}")
        @Pattern(
            regexp = "^(https?|ftp)://[^\s/$.?#].[^\s]*$",
            message = "{must_be_a_valid_link}"
        )
        String link

) {}
