package com.joeun.config;

import com.joeun.service.rental.ExpireEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories
public class RedisEventConfig {

    private final RedisConnectionFactory connectionFactory;
    private final ExpireEventListener expireEventListener;

    @Bean
    public RedisMessageListenerContainer keyEventListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(expireEventListener,
                new PatternTopic("__keyevent@0__:expired"));
        return container;
    }
}
