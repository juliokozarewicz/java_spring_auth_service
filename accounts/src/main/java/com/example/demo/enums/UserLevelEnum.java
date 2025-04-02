package com.example.demo.enums;

public enum UserLevelEnum {

    USER("user"),
    MODERATOR("moderator"),
    ADMINISTRATOR("administrator");

    private final String description;

    UserLevelEnum(String description) {
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
