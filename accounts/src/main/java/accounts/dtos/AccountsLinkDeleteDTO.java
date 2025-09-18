package accounts.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AccountsLinkDeleteDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^(https?|ftp)://[^ /$.?#].[^ ]*$",
        message = "{validation_valid_link}"
    )
    String link

) {}