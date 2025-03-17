package com.example.demo.interfaces;

public interface AccountsInterface {

    // commons
    void sendVerificationByEmail();

    // methods
    void createAccount();
    void activateEmail();
    void updatePasswordLink();
    void updatePassword();
    void login();
    void refreshLogin();
    void getProfile();
    void updateProfile();
    void updateEmailLink();
    void updateEmail();
    void deleteAccountLink();
    void deleteAccount();

}