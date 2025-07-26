package accounts.enums;

public enum EmailResponsesEnum {

    ACTIVATE_ACCOUNT_SUCCESS("email_activate_account_click"),
    ACCOUNT_EXIST_ACTIVATED_ERROR("email_account_exist_activated_error"),
    ACCOUNT_EXIST_DEACTIVATED_ERROR("email_account_exist_deactivated_error"),
    ACCOUNT_BANNED_ERROR("email_account_banned_error"),
    UPDATE_PASSWORD_CLICK("email_reset_password_click");

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
