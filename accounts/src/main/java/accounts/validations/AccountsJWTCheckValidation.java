package accounts.validations;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AccountsJWTCheckValidation(

    @NotEmpty(message = "{validation_is_required}")
    @Pattern(
        regexp = "^[A-Za-z0-9_-]+$",
        message = "{response_invalid_credentials}"
    )
    String accessToken

) {}