package accounts.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsLinkUpdatePasswordDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 255, message = "{validation_many_characters}")
    @Email(message = "{validation_must_valid_email}")
    String email,

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^(https?://)(?!localhost|((127|0)\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(10|192\\.168|169\\.254|172\\.(1[6-9]|2[0-9]|3[0-1]))\\.\\d{1,3}\\.\\d{1,3})\\S+$",
        message = "{validation_valid_link}"
    )
    String link

) {}