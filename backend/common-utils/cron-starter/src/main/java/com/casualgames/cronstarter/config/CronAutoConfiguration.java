package com.casualgames.cronstarter.config;

import com.casualgames.cronstarter.controller.CronController;
import com.casualgames.cronstarter.service.CronRunService;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
@Import({
        CronRunService.class,
        CronController.class
})
public class CronAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory,
                                     @Value("${spring.application.name}") String applicationName) {
        return new RedisLockProvider(redisConnectionFactory, applicationName);
    }
}
