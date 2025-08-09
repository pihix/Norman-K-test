package com.nimbleways.springboilerplate.transverse.flowtests;

import com.nimbleways.springboilerplate.testhelpers.baseclasses.BaseApplicationTestsWithDb;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthenticationFlowApplicationTests extends BaseApplicationTestsWithDb {

    @Test
    void signup_login_getUsers_refreshToken() {
        // ----------- Getting users without being logged in ------------//
        webTestClient.get().uri("/users").exchange().expectStatus().isUnauthorized();

        // ----------- Sign up new ADMIN user ------------//
        webTestClient
            .post()
            .uri("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {"name":"Name", "username":"admin1",
                "password":"password1", "roles":["ADMIN"]}"""
            )
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("$.username")
            .isEqualTo("admin1");

        // ----------- login ------------//
        webTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {"username":"admin1", "password":"password1"}"""
            )
            .exchange()
            .expectStatus()
            .isOk();

        // ----------- Getting users  ------------//
        webTestClient
            .get()
            .uri("/users")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$[0].username")
            .isEqualTo("admin1");

        // ----------- refresh token ------------//
        webTestClient
            .post()
            .uri("/auth/refreshToken")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk();

        // ----------- logout ------------//
        webTestClient
            .post()
            .uri("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk();

        // ----------- Getting users without being logged in ------------//
        webTestClient.get().uri("/users").exchange().expectStatus().isUnauthorized();
    }
}
