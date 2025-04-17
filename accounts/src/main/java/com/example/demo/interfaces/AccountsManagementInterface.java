package com.example.demo.interfaces;

public interface AccountsManagementInterface {

    String createToken(String email, String reason);

    String createCredentialJWT(String email);

    String createRefreshLogin(String email);

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