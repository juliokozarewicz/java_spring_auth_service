package accounts.services;

import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsProfileRepository;
import accounts.persistence.repositories.AccountsRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountsDeleteRedisListenerService implements MessageListener {

    private final AccountsRepository accountsRepository;
    private final AccountsProfileRepository accountsProfileRepository;

    public AccountsDeleteRedisListenerService (

        AccountsRepository accountsRepository,
        AccountsProfileRepository accountsProfileRepository

    ) {

        this.accountsRepository = accountsRepository;
        this.accountsProfileRepository = accountsProfileRepository;

    }

    // Delete account (user decision)
    @Override
    public void onMessage(Message message, byte[] pattern) {

        String expiredKey  = new String(message.getBody());
        UUID idUser = UUID.fromString(expiredKey.substring(expiredKey.indexOf("::") + 2));


        if (expiredKey.startsWith("deletedAccountByUserCache")) {

            System.out.println(expiredKey);

        }

        // Delete account not activated
        if (expiredKey.startsWith("notActivatedAccountCache")) {

            // Get user account
            Optional<AccountsEntity> findUser =  accountsRepository.findById(
                idUser
            );

            // Get user profile
            Optional<AccountsProfileEntity> findProfile = accountsProfileRepository
                .findById(idUser);

            // Delete both
            accountsRepository.delete(findUser.get());
            accountsProfileRepository.delete(findProfile.get());

        }

    }

}