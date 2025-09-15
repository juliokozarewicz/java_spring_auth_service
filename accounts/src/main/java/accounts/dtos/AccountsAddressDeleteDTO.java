package accounts.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record AccountsAddressDeleteDTO(


    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^[0-9]{1,100}$",
        message = "{validation_invalid_id}"
    )
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    UUID addressId

) {}