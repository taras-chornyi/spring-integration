package com.taras.chornyi.spring.integration;

import com.taras.chornyi.spring.integration.config.ApplicationProperties;
import com.taras.chornyi.spring.integration.service.RestGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptAllFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller(@Value("${application.poller.delay}") String poller) {
        return Pollers.fixedRate(Integer.parseInt(poller)).get();
    }

    @Bean
    public MessageSource<File> fileMessageSource(
            @Value("${application.dirToScan}") String dirToScan,
            @Value("${application.fileRegex}") String regex) {
        FileReadingMessageSource fileReadingMessageSource = new FileReadingMessageSource();
        fileReadingMessageSource.setDirectory(new File(dirToScan));
        CompositeFileListFilter filter = new CompositeFileListFilter(
                Arrays.asList(new AcceptAllFileListFilter(),
                        new RegexPatternFileListFilter(regex))
        );
        fileReadingMessageSource.setFilter(filter);
        return fileReadingMessageSource;
    }

    @Bean
    public MessageHandler restGateway(
            @Value("${application.url}") String url,
            @Value("${application.dirToMove}") String dirToMove) {
        log.info("Prepare multipart request");
        return new RestGateway(url, new RestTemplate(), dirToMove);
    }

    @Bean
    public IntegrationFlow httpFlow(MessageHandler restGateway,
                                    @Value("${application.poller.delay}") long period,
                                    @Value("${application.poller.maxMessagesPerPoll}")
                                            int maxMessagesPerPoll,
                                    MessageSource fileReadingMessageSource) {
        return IntegrationFlows.from(fileReadingMessageSource,
                c -> c.poller(Pollers.fixedDelay(period)
                        .maxMessagesPerPoll(maxMessagesPerPoll)))
                .handle(restGateway)
                .get();
    }

}
