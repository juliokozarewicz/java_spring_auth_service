package accounts.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsActivateDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 5000 , message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[A-Za-z0-9_-]+$",
        message = "{validation_disallowed_characters}"
    )
    String email,

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String token

) {}