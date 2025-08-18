package accounts.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsLinkUpdateEmailDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 255, message = "{validation_many_characters}")
    @Email(message = "{validation_must_valid_email}")
    String newEmail,

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^(https?|ftp)://[^\s/$.?#].[^\s]*$",
        message = "{validation_valid_link}"
    )
    String link

) {}