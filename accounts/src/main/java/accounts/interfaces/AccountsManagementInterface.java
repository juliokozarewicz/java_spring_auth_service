package accounts.interfaces;

import java.sql.Timestamp;
import java.time.Instant;

public interface AccountsManagementInterface {

    Long createUniqueId();

    String createVerificationToken(Long idUser, String reason);

    String createVerificationPin(Long idUser, String reason, Object meta);

    void deletePinByIdUser(Long idUser);

    void deleteAllVerificationTokenByIdUserNewTransaction(Long idUser);

    String createCredentialJWT(String email);

    String createRefreshLogin(
        Long idUser,
        String userIp,
        String userAgent,
        Instant createdAt
    );

    void deleteOneRefreshLogin(Long idUser, String refreshToken);

    void deleteAllRefreshTokensByIdNewTransaction(Long idUser);

    void deleteExpiredRefreshTokensListById(Long idUser);

    void sendEmailStandard(String email, String message, String link);

    void createUserLog(
        String ipAddress,
        Long idUser,
        String agent,
        String updateType,
        String oldValue,
        String newValue
    );

    void enableAccount(Long idUser);

    void disableAccount(Long idUser);

}