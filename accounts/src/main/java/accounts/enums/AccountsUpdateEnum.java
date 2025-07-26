package accounts.enums;

public enum AccountsUpdateEnum {

    ACTIVATE_ACCOUNT("activate-account"),
    UPDATE_PASSWORD("update-password");

    private final String description;

    AccountsUpdateEnum(String description) {
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
