package accounts.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountsAddressGetDTO {

    private UUID addressId;
    private String addressName;
    private String zipCode;
    private String street;
    private String number;
    private String addressLineTwo;
    private String neighborhood;
    private String city;
    private String state;
    private String country;
    private String addressType;
    private Boolean isPrimary;
    private String landmark;

}