package com.mqb.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {
    @Value("${elasticsearch.host.first}")
    private String ES_IP_O1;
    @Value("${elasticsearch.host.second}")
    private String ES_IP_O2;
    @Value("${elasticsearch.host.third}")
    private String ES_IP_O3;
    @Value("${elasticsearch.port.first}")
    private int ES_PORT_01;
    @Value("${elasticsearch.port.second}")
    private int ES_PORT_02;
    @Value("${elasticsearch.port.third}")
    private int ES_PORT_03;
    @Value("${elasticsearch.protocol}")

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ES_IP_O1, ES_PORT_01, "http"),
                        new HttpHost(ES_IP_O2, ES_PORT_02, "http"),
                        new HttpHost(ES_IP_O3, ES_PORT_03, "http")));
        return client;
    }
}
