package com.mqb.config;

import com.mqb.properties.EsProperties;
import com.mqb.service.MyEsTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnBean(annotation = EnableEsConfiguration.class)
@EnableConfigurationProperties(EsProperties.class)
@Configuration
public class EsAutoConfiguration {

    @Bean
    public MyEsTemplate myEsTemplate(){
        return new MyEsTemplate();
    }
}
