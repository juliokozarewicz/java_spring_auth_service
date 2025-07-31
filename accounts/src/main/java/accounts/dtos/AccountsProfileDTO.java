package accounts.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountsProfileDTO {

    private String profileImage;
    private String name;
    private String phone;
    private String identityDocument;
    private String gender;
    private String birthdate;
    private String biography;
    private String language;

}