package accounts.interfaces;

public interface AccountsManagementInterface {

    String createVerificationToken(String email, String reason);

    void deleteAllVerificationTokenByEmailNewTransaction(String email);

    String createCredentialJWT(String email);

    String createRefreshLogin(String userIp, String userAgent, String email);

    void deleteRefreshLoginByToken(String refreshToken);

    void deleteAllRefreshTokensByEmailNewTransaction(String email);

    void sendEmailStandard(String email, String message, String link);

    void createUserLog(
        String ipAddress,
        String userId,
        String agent,
        String updateType,
        String oldValue,
        String newValue
    );

    void enableAccount(String userId);

    void disableAccount(String userId);

}