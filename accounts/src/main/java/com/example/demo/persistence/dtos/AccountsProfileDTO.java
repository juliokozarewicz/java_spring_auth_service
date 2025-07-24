package com.example.demo.persistence.dtos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AccountsProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String profileImage;
    private String name;
    private String phone;
    private String identityDocument;
    private String gender;
    private String birthdate;
    private String biography;
    private String language;

}