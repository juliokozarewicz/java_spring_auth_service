package email_management_service.validations;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SendEmailDataValidation(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 255, message = "{validation_many_characters}")
    @Email(message = "{validation_must_valid_email}")
    String recipient,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 256, message = "{validation_many_characters}")
    String subject,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 512, message = "{validation_many_characters}")
    String message

) {}