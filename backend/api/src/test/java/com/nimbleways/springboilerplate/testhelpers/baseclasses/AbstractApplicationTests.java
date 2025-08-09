package com.nimbleways.springboilerplate.testhelpers.baseclasses;

import com.nimbleways.springboilerplate.testhelpers.utils.CookieStore;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.UseMainMethod;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, useMainMethod = UseMainMethod.ALWAYS)
public abstract class AbstractApplicationTests {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path}")
    protected String contextPath;

    protected String baseUrl;
    protected WebTestClient webTestClient;

    protected AbstractApplicationTests() {}

    @PostConstruct
    private void postConstruct() {
        this.baseUrl = "http://localhost:%d%s".formatted(port, contextPath);
        this.webTestClient = WebTestClient.bindToServer()
            .baseUrl(baseUrl)
            .filter(new CookieStore().getFilter())
            .responseTimeout(Duration.ofMinutes(5))
            .build();
    }
}
