package com.example.demo.interfaces;

public interface AccountsManagementInterface {
    void enableAccount(String userId);
    void disableAccount(String userId);
    String createToken(String email, String reason);
    void sendEmailLink(String email, String link, String message);
    void sendEmailActivatedAccount(String email);
}