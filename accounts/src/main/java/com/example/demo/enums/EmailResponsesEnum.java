package com.example.demo.enums;

public enum EmailResponsesEnum {

    SUCCESS_ACTIVATE_ACCOUNT("activate_account"),
    SUCCESS_UPDATE_PASSWORD("reset_password"),
    ERROR_ACCOUNT_EXIST_ACTIVATED("account_exist_activated"),
    ERROR_ACTIVATE_EMAIL("activate_email_error");

    private final String description;

    EmailResponsesEnum(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public String getDescription() {
        return this.description;
    }

}
