package com.example.demo.interfaces;

public interface AccountsManagementInterface {
    void enableAccount(String activeId);
    void disableAccount(String activeId);
    String createToken(String email, String reason);
}