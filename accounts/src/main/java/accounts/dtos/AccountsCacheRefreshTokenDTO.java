package accounts.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountsCacheRefreshTokenDTO {

    private String userIp;
    private String userAgent;
    private String userEmail;

}