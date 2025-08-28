package accounts.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsUpdateEmailDTO(

    @NotEmpty(message = "{validation_is_required}")
    String password,

    @NotEmpty(message = "{validation_is_required}")
    String pin,

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String token

) {}
