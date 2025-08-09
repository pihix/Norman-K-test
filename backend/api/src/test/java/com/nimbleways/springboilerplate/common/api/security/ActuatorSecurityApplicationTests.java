package com.nimbleways.springboilerplate.common.api.security;

import static com.nimbleways.springboilerplate.testhelpers.utils.JsonUtils.jsonIgnoreArrayOrder;

import com.nimbleways.springboilerplate.testhelpers.baseclasses.BaseApplicationTestsWithoutDb;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.reactive.server.WebTestClient;

// This is an ApplicationTests because I didn't manage to make mockmvc return a problemdetails when access denied
class ActuatorSecurityApplicationTests extends BaseApplicationTestsWithoutDb {

    @Test
    void get_on_actuator_base_path_with_good_username_and_password_returns_200(
        @Value("${management.server.user}") String username,
        @Value("${management.server.password}") String password
    ) {
        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient
            .get()
            .uri("/actuatorz")
            .header("Authorization", baseAuthHeaderValue(username, password))
            .exchange();

        responseSpec
            .expectStatus()
            .isOk()
            .expectBody()
            .json(
                """
                {"_links":{"self":{"href":"%s/actuatorz","templated":false},
                "info":{"href":"%s/actuatorz/info","templated":false}}}""".formatted(baseUrl, baseUrl),
                jsonIgnoreArrayOrder
            );
    }

    @Test
    void get_on_actuator_base_path_with_good_username_and_bad_password_returns_401_and_WWW_Authenticate_header(
        @Value("${management.server.user}") String username
    ) {
        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient
            .get()
            .uri("/actuatorz")
            .header("Authorization", baseAuthHeaderValue(username, "bad_password"))
            .exchange();

        responseSpec
            .expectStatus()
            .isUnauthorized()
            .expectHeader()
            .valueEquals("www-authenticate", "Basic realm=\"Actuator\"")
            .expectBody()
            .json(
                """
                {"type" : "about:blank", "title" : "errors.unauthorized", "status" : 401,
                 "detail" : "errors.unauthorized", "instance" : "%s/actuatorz"}""".formatted(contextPath),
                jsonIgnoreArrayOrder
            );
    }

    @Test
    void get_on_actuator_base_path_with_bad_username_and_password_returns_401_and_WWW_Authenticate_header() {
        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient
            .get()
            .uri("/actuatorz")
            .header("Authorization", baseAuthHeaderValue("bad_user", "bad_password"))
            .exchange();

        responseSpec
            .expectStatus()
            .isUnauthorized()
            .expectHeader()
            .valueEquals("www-authenticate", "Basic realm=\"Actuator\"")
            .expectBody()
            .json(
                """
                {"type" : "about:blank", "title" : "errors.unauthorized", "status" : 401,
                 "detail" : "errors.unauthorized", "instance" : "%s/actuatorz"}""".formatted(contextPath),
                jsonIgnoreArrayOrder
            );
    }

    @Test
    void get_on_actuator_base_path_without_username_and_password_returns_401_and_WWW_Authenticate_header() {
        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient.get().uri("/actuatorz").exchange();

        responseSpec
            .expectStatus()
            .isUnauthorized()
            .expectHeader()
            .valueEquals("www-authenticate", "Basic realm=\"Actuator\"")
            .expectBody()
            .json(
                """
                {"type" : "about:blank", "title" : "errors.unauthorized", "status" : 401,
                 "detail" : "errors.unauthorized", "instance" : "%s/actuatorz"}""".formatted(contextPath),
                jsonIgnoreArrayOrder
            );
    }

    private static String baseAuthHeaderValue(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
