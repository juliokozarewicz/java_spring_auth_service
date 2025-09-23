package accounts.services;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class AccountsDeleteRedisListenerService implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String expiredKey = new String(message.getBody());

        if (expiredKey.startsWith("deletedAccountByUserCache")) {

            System.out.println(expiredKey);

        }

        if (expiredKey.startsWith("notActivatedAccountCache")) {

            System.out.println(expiredKey);

        }

    }

}