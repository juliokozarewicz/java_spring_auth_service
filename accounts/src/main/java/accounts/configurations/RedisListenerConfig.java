package accounts.configurations;

import accounts.services.AccountsDeleteRedisListenerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {

    private final AccountsDeleteRedisListenerService redisKeyExpirationListener;

    public RedisListenerConfig(AccountsDeleteRedisListenerService redisKeyExpirationListener) {
        this.redisKeyExpirationListener = redisKeyExpirationListener;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisKeyExpirationListener, new PatternTopic(
            "__keyevent@0__:expired")
        );

        return container;

    }

}