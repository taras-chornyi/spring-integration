package com.taras.chornyi.spring.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties are configured in the application.yml file.
 *
 * @author Taras Chornyi
 */
@Data
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String url;
    private String dirToScan;
    private String fileRegex;
    private String dirToMove;
    private Poller poller = new Poller();

    @Data
    public static class Poller {
        private String delay;
        private String maxMessagesPerPoll;
    }

}

