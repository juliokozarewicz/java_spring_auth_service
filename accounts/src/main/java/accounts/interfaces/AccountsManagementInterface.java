package accounts.interfaces;

public interface AccountsManagementInterface {

    String createVerificationToken(String email, String reason);

    String createVerificationPin(String idUser, String reason);

    void deleteAllVerificationPinByUserId(String idUser);

    void deleteAllVerificationTokenByEmailNewTransaction(String email);

    String createCredentialJWT(String email);

    String createRefreshLogin(String idUser, String userIp, String userAgent);

    void deleteOneRefreshLogin(String idUser, String refreshToken);

    void deleteAllRefreshTokensByIdNewTransaction(String userId);

    void cleanUserRefreshTokensList(String userId);

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