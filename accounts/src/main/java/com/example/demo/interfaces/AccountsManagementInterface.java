package com.example.demo.interfaces;

import org.springframework.http.ResponseEntity;

public interface AccountsManagementInterface {
    void enableAccount(String userId);
    void disableAccount(String userId);
    void statusActivatedAccount(String email);
    String createToken(String email, String reason);
}