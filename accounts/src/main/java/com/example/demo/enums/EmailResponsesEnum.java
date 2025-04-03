package com.example.demo.enums;

public enum EmailResponsesEnum {

    ACTIVATE_ACCOUNT_SUCCESS("_email_activate_account_click"),
    ACCOUNT_EXIST_ACTIVATED_ERROR("_email_account_exist_activated_error"),
    UPDATE_PASSWORD_CLICK("_email_reset_password_click");

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
