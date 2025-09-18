package accounts.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsCreateDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 255, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String name,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 255, message = "{validation_many_characters}")
    @Email(message = "{validation_must_valid_email}")
    String email,

    @NotEmpty(message = "{validation_is_required}")
    @Size(
        min = 8, max = 255,
        message = "{validation_must_eight_characters_long}"
    )
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
        message = "{validation_must_uppercase_letter}"
    )
    String password,

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^(https?|ftp)://[^ /$.?#].[^ ]*$",
        message = "{validation_valid_link}"
    )
    String link

) {}
