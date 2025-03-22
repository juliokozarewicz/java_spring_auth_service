package com.example.demo.services;

import com.example.demo.interfaces.AccountsManagementInterface;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes

    // Constructor
    public AccountsManagementService () {}

    @Override
    public void enableAccount(String activeId) {
    }

    @Override
    public void disableAccount(String activeId) {
    }

    @Override
    public String createToken(String reason) {

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        return "token-generated-code";

    }

}
