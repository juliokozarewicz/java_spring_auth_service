package accounts.persistence.dtos;

public record SendEmailDataDTO(
    String recipient,
    String subject,
    String message
) {}
