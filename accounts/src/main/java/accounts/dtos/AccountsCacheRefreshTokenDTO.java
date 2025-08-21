package accounts.dtos;

public record AccountsCacheRefreshTokenDTO(

    String userIp,
    String userAgent,
    String email

) {}