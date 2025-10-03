package accounts.services;

import accounts.persistence.entities.AccountsDeletedEntity;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsDeletedRepository;
import accounts.persistence.repositories.AccountsProfileRepository;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountsDeleteRedisListenerService implements MessageListener {

    private final AccountsRepository accountsRepository;
    private final AccountsProfileRepository accountsProfileRepository;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;
    private final AccountsDeletedRepository accountsDeletedRepository;

    public AccountsDeleteRedisListenerService (

        AccountsRepository accountsRepository,
        AccountsProfileRepository accountsProfileRepository,
        AccountsManagementService accountsManagementService,
        EncryptionService encryptionService,
        AccountsDeletedRepository accountsDeletedRepository

    ) {

        this.accountsRepository = accountsRepository;
        this.accountsProfileRepository = accountsProfileRepository;
        this.accountsManagementService = accountsManagementService;
        this.encryptionService = encryptionService;
        this.accountsDeletedRepository = accountsDeletedRepository;

    }

    // Delete account (user decision)
    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {

        String expiredKey  = new String(message.getBody());
        UUID idUser = UUID.fromString(expiredKey.substring(expiredKey.indexOf("::") + 2));

        if (expiredKey.startsWith("deletedAccountByUserCache")) {

            // Get user account
            Optional<AccountsEntity> findUser =  accountsRepository.findById(
                idUser
            );

            if (findUser.isPresent()) {

                // Create deleted account (id user for id and email)
                AccountsDeletedEntity newDeletedAccount = new AccountsDeletedEntity();
                newDeletedAccount.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
                newDeletedAccount.setId(findUser.get().getId());
                newDeletedAccount.setEmail(findUser.get().getEmail());
                accountsDeletedRepository.save(newDeletedAccount);

                // Change email for user id
                findUser.get().setEmail("deleted-" + findUser.get().getId().toString());
                findUser.get().setPassword(
                    encryptionService.hashPassword(
                        accountsManagementService.createUniqueId().toString()
                    )
                );
                accountsRepository.save(findUser.get());

            }

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

            if ( findUser.isPresent() && findProfile.isPresent() ) {

                // Delete both
                accountsRepository.delete(findUser.get());
                accountsProfileRepository.delete(findProfile.get());

            }

        }

    }

}