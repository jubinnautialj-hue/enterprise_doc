package com.enterprise.doc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "onlyoffice.document-server")
public class OnlyOfficeConfig {

    private String url;
    private String secret;

    public String getApiUrl() {
        return url + "/web-apps/apps/api/documents/api.js";
    }

    public String getCommandUrl() {
        return url + "/coauthoring/CommandService.ashx";
    }
}
