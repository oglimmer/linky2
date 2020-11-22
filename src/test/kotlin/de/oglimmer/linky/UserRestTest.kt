package de.oglimmer.linky

import de.oglimmer.linky.rest.UserRequestParam
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@TestMethodOrder(MethodOrderer.MethodName::class)
class UserRestTest(@Autowired private val webTestClient: WebTestClient) : AbstractMongoIntegrationTest() {

    @Test
    fun test1CreateUser() {
        val userRequestParam = UserRequestParam("foo@bar.com", "secret")
        webTestClient.post().uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(userRequestParam), userRequestParam.javaClass)
                .exchange()
                .expectStatus().isCreated
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty
    }

    @Test
    fun test2CreateUserAgain() {
        val userRequestParam = UserRequestParam("foo@bar.com", "different-secret")
        webTestClient.post().uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(userRequestParam), userRequestParam.javaClass)
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    fun test3AuthUser() {
        val userRequestParam = UserRequestParam("foo@bar.com", "secret")
        webTestClient.post().uri("/v1/users/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(userRequestParam), userRequestParam.javaClass)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty
    }

    @Test
    fun test4FailedAuthUser() {
        val userRequestParam = UserRequestParam("foo@bar.com", "wrong-password")
        webTestClient.post().uri("/v1/users/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(userRequestParam), userRequestParam.javaClass)
                .exchange()
                .expectStatus().isUnauthorized
    }

}

