package com.taras.chornyi.spring.integration;

import com.taras.chornyi.spring.integration.service.RestGateway;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static com.taras.chornyi.spring.integration.service.RestGateway.SUCCESSFULLY_UPLOADED;

@ContextConfiguration(classes = Application.class)
@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

    private static final String URI = "http://localhost/test";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    @Spy
    RestGateway restGateway;

    private File payload;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void init() throws IOException {
        Whitebox.setInternalState(restGateway, "uri", URI);
        Whitebox.setInternalState(restGateway, "dirToMove", "build/");
        this.payload = tempFolder.newFile("sample.bdt");
    }

    @Test
    @DirtiesContext
    public void testGatewayAccepted() throws Exception{
        assertTrue(payload.exists());

        Message<?> message = getMessage();

        PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

        // Build the response with required values
        ResponseEntity<String> respEntity =
                new ResponseEntity(SUCCESSFULLY_UPLOADED + payload.getName(), HttpStatus.ACCEPTED);
        setExpectationOnMockRestTemplate(respEntity);

        Object response = restGateway.handleRequestMessage(message);
        assertNull(response);

        verify(restGateway, times(1)).moveFile(payload);
    }

    @Test
    @DirtiesContext
    public void testGatewayNotResponse() throws Exception{
        assertTrue(payload.exists());

        Message<?> message = getMessage();

        PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

        // Build the response with required values
        ResponseEntity<String> respEntity = new ResponseEntity("No content", HttpStatus.BAD_REQUEST);

        // Set expectation on mock RestTemplate
        setExpectationOnMockRestTemplate(respEntity);

        Object response = restGateway.handleRequestMessage(message);
        assertNull(response);

        verify(restGateway, times(0)).moveFile(payload);
    }

    private Message<?> getMessage() {
        return new Message<Object>() {
            @Override
            public Object getPayload() {
                return payload;
            }
            @Override
            public MessageHeaders getHeaders() {
                Map<String, Object> headers = new HashMap<>();
                headers.put("id", "123-123-123");
                headers.put("timestamp", LocalDateTime.now());
                return new MessageHeaders(headers);
            }
        };
    }

    private void setExpectationOnMockRestTemplate(ResponseEntity<String> respEntity) {
        // Set expectation on mock RestTemplate
        PowerMockito.when(restTemplate.exchange(
                Matchers.any(URI.class),
                Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<LinkedMultiValueMap<String, Object>>> any(),
                Matchers.any(Class.class))
        ).thenReturn(respEntity);
    }

}
