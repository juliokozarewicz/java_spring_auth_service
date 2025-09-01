package accounts.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountsConnectedDevicesGetDTO {

    private String deviceName;
    private String country;
    private String regionName;
    private String city;
    private String lat;
    private String lon;

}