package com.mqb.properties;

import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@ConfigurationProperties(prefix = "es")
@Component
@Data
public class EsProperties {
    private String hosts;
    private String ports;

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        if(StringUtils.isEmpty(hosts)){
            throw new RuntimeException("es hosts配置为空");
        }
        if(StringUtils.isEmpty(ports)){
            throw new RuntimeException("es ports配置为空");
        }
        String[] hostsArray = hosts.split(",");
        String[] portsArray = ports.split(",");
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(hostsArray[0], Integer.valueOf(portsArray[0]), "http"),
                        new HttpHost(hostsArray[1], Integer.valueOf(portsArray[1]), "http"),
                        new HttpHost(hostsArray[2], Integer.valueOf(portsArray[2]), "http")));
        return client;
    }

}
