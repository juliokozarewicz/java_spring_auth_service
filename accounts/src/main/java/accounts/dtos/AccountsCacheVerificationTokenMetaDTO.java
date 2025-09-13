package accounts.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountsCacheVerificationTokenMetaDTO {

    private String verificationToken;
    private String reason;

}