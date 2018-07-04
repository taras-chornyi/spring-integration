package com.taras.chornyi.spring.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RestGateway extends AbstractReplyProducingMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(RestGateway.class);

    public static final String SUCCESSFULLY_UPLOADED = "Successfully uploaded: ";

    private final String uri;
    private final RestTemplate restTemplate;
    private final String dirToMove;

    public RestGateway(String uri, RestTemplate restTemplate, String dirToMove) {
        this.uri = uri;
        this.restTemplate = restTemplate;
        this.dirToMove = dirToMove;
    }

    @Override
    public Object handleRequestMessage(Message<?> message) {
        String fileName = message.getPayload().toString();
        File file = new File(fileName);
        if (file.exists()) {
            log.info("Gateway message handler get {}" + fileName);

            try {
                ResponseEntity<String> result = restTemplate
                        .exchange(getUri(), HttpMethod.POST, getRequestEntity(fileName), String.class);
                if (isAccepted(result, file)) {
                    moveFile(file);
                }
            } catch (ResourceAccessException e) {
                log.info(e.getMessage());
            }
        }
        return null;
    }

    private URI getUri() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        return builder.build().encode().toUri();
    }

    private HttpEntity<LinkedMultiValueMap<String, Object>> getRequestEntity(String fileName) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new FileSystemResource(fileName));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(map, headers);
    }

    private boolean isAccepted(ResponseEntity<String> result, File file) {
        return result != null
                && result.getStatusCode() == HttpStatus.ACCEPTED
                && (SUCCESSFULLY_UPLOADED + file.getName()).equals(result.getBody());
    }

    public void moveFile(File file) {
        try {
            if (!Files.exists(Paths.get(dirToMove))) {
                Files.createDirectory(Paths.get(dirToMove));
            }
            Files.move(file.toPath(), Paths.get(dirToMove + file.getName()), REPLACE_EXISTING);
        } catch (IOException e) {
            log.info(e.getMessage(), e);
        }
    }
}
