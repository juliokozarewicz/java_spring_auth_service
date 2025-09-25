package accounts.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AccountsLinkDeleteDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^(https?://)(?!localhost|((127|0)\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(10|192\\.168|169\\.254|172\\.(1[6-9]|2[0-9]|3[0-1]))\\.\\d{1,3}\\.\\d{1,3})\\S+$",
        message = "{validation_valid_link}"
    )
    String link

) {}