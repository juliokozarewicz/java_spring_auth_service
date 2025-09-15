package accounts.interfaces;

import java.time.Instant;
import java.util.UUID;

public interface AccountsManagementInterface {

    UUID createUniqueId();

    String createVerificationToken(UUID idUser, String reason);

    String createVerificationPin(UUID idUser, String reason, Object meta);

    void deletePinByIdUser(UUID idUser);

    void deleteAllVerificationTokenByIdUserNewTransaction(UUID idUser);

    String createCredentialJWT(String email);

    String createRefreshLogin(
        UUID idUser,
        String userIp,
        String userAgent,
        Instant createdAt
    );

    void deleteOneRefreshLogin(UUID idUser, String refreshToken);

    void deleteAllRefreshTokensByIdNewTransaction(UUID idUser);

    void deleteExpiredRefreshTokensListById(UUID idUser);

    void sendEmailStandard(String email, String message, String link);

    void createUserLog(
        String ipAddress,
        UUID idUser,
        String agent,
        String updateType,
        String oldValue,
        String newValue
    );

    void enableAccount(UUID idUser);

    void disableAccount(UUID idUser);

}