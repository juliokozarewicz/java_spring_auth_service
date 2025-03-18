package com.example.demo.interfaces;

public interface AccountsInterface {

    // Commons
    void activateEmail(String activeId);

    // Account management
    void createAccountRequest();
    void activateEmailRequest();
    void updatePasswordLink();
    void updatePasswordRequest();
    void loginRequest();
    void refreshLoginRequest();
    void getProfileRequest();
    void updateProfileRequest();
    void updateEmailLink();
    void updateEmailRequest();
    void deleteAccountLink();
    void deleteAccountRequest();

}