package com.sujeet.projects.rate_limiter.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Getter
@PropertySource(value = "${secrets.filePath}", factory = YamlPropertySourceFactory.class)
public class SecretProperties {

    @Value("${redis.userName}")
    private String redisUsername;

    @Value("${redis.password}")
    private String redisPassword;


}
