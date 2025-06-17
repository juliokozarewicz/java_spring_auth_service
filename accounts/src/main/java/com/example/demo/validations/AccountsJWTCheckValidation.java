package com.example.demo.validations;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AccountsJWTCheckValidation(

        @NotEmpty(message = "{validation_is_required}")
        @Pattern(
            regexp = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$",
            message = "{response_invalid_credentials}"
        )
        String accessToken

) {}