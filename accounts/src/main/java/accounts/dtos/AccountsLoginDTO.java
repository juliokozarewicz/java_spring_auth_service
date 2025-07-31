package accounts.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsLoginDTO(

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
    String password

) {}