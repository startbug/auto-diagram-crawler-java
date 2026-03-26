package com.exportbot.crawler.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppConfig.class)
public class ConfigProperties {

    @Bean
    public ConfigLoader configLoader() {
        return new ConfigLoader();
    }
}
