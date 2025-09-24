package accounts.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountsProfileUpdateDTO(

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 255, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^[^<>&'\"/]*$",
        message = "{validation_disallowed_characters}"
    )
    String name,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 25, message = "{validation_many_characters}")
    @Pattern(regexp = "^[+\\d()\\s-]*$", message = "{validation_disallowed_characters}")
    String phone,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(regexp = "^[^<>&'\"/]*$", message = "{validation_disallowed_characters}")
    String identityDocument,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(regexp = "^[^<>&'\"/]*$", message = "{validation_disallowed_characters}")
    String gender,

    @Size(min = 1, message = "{validation_is_required}")
    @Pattern(
        regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
        message = "{validation_birthdate}"
    )
    String birthdate,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    @Pattern(regexp = "^[^<>&'\"/]*$", message = "{validation_disallowed_characters}")
    String biography,

    @Size(min = 1, message = "{validation_is_required}")
    @Size(max = 50, message = "{validation_many_characters}")
    @Pattern(regexp = "^[a-zA-Z-]*$", message = "{validation_disallowed_characters}")
    String language

) {}