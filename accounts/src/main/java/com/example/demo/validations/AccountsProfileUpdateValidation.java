package com.example.demo.validations;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AccountsProfileUpdateValidation(

    @Size(max = 555, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
        message = "{validation_invalid_url}"
    )
    String profileImage,

    @Size(max = 255, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]+$",
        message = "{validation_disallowed_characters}"
    )
    String name,

    @Size(max = 25, message = "{validation_many_characters}")
    @Pattern(regexp = "^[+\\d()\\s-]*$", message = "{validation_disallowed_characters}")
    String phone,

    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(regexp = "^[^<>&'\"/]*$", message = "{validation_disallowed_characters}")
    String identityDocument,

    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(regexp = "^[^<>&'\"/]*$", message = "{validation_disallowed_characters}")
    String gender,

    @Past(message = "{#####validation_birthdate_past}")
    LocalDate birthdate,

    @Size(max = 50, message = "{validation_many_characters}")
    @Pattern(regexp = "^[a-zA-Z-]*$", message = "{validation_disallowed_characters}")
    String language

) {
}