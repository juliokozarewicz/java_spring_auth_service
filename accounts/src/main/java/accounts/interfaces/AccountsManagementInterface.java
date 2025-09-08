package accounts.interfaces;

import java.sql.Timestamp;

public interface AccountsManagementInterface {

    String createVerificationToken(String idUser, String reason);

    String createVerificationPin(String idUser, String reason, Object meta);

    void deletePinByIdUser(String idUser);

    void deleteAllVerificationTokenByIdUserNewTransaction(String idUser);

    String createCredentialJWT(String email);

    String createRefreshLogin(
        String idUser,
        String userIp,
        String userAgent,
        Timestamp createdAt
    );

    void deleteOneRefreshLogin(String idUser, String refreshToken);

    void deleteAllRefreshTokensByIdNewTransaction(String idUser);

    void deleteExpiredRefreshTokensListById(String idUser);

    void sendEmailStandard(String email, String message, String link);

    void createUserLog(
        String ipAddress,
        String idUser,
        String agent,
        String updateType,
        String oldValue,
        String newValue
    );

    void enableAccount(String idUser);

    void disableAccount(String idUser);

}