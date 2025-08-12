package accounts.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountsAddressGetDTO {

    private String addressId;
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