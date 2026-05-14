package com.enterprise.doc.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        HttpHost[] hosts = parseUris(uris);

        RestClientBuilder builder = RestClient.builder(hosts);

        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    private HttpHost[] parseUris(String uris) {
        String[] uriArray = uris.split(",");
        HttpHost[] hosts = new HttpHost[uriArray.length];
        for (int i = 0; i < uriArray.length; i++) {
            String uri = uriArray[i].trim();
            if (uri.startsWith("http://")) {
                uri = uri.substring(7);
            } else if (uri.startsWith("https://")) {
                uri = uri.substring(8);
            }
            String[] parts = uri.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
            hosts[i] = new HttpHost(host, port, "http");
        }
        return hosts;
    }
}
