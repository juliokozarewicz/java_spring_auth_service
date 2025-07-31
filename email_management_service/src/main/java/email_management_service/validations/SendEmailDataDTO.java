package email_management_service.validations;

public record SendEmailDataDTO(
    String recipient,
    String subject,
    String message
) {}
