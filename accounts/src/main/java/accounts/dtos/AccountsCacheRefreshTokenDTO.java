package accounts.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountsCacheRefreshTokenDTO {

    private UUID idUser;
    private String userIp;
    private String userAgent;
    private Instant createdAt;

}