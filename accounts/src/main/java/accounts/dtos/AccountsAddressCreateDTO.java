package accounts.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsAddressCreateDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String addressName,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 50, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String zipCode,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String street,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 50, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String number,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String addressLineTwo,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String neighborhood,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String city,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String state,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String country,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String addressType,

    @NotNull(message = "{validation_is_required}")
    Boolean isPrimary,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String landmark

) {}