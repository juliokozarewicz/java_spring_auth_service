package accounts.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountsCacheVerificationPinMetaDTO {

    private String verificationPin;
    private String reason;
    private Object meta;

}