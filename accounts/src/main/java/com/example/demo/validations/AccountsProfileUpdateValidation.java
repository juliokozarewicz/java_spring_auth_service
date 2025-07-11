package com.example.demo.validations;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AccountsProfileUpdateValidation(

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

    @Pattern(
        regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
        message = "{validation_birthdate}"
    )
    String birthdate,

    @Size(max = 50, message = "{validation_many_characters}")
    @Pattern(regexp = "^[a-zA-Z-]*$", message = "{validation_disallowed_characters}")
    String language

) {}