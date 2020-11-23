package de.oglimmer.linky

import de.oglimmer.linky.rest.UserRequestParam
import de.oglimmer.linky.rest.UserResponse
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@TestMethodOrder(MethodOrderer.MethodName::class)
class TagsRestTest(@Autowired private val webTestClient: WebTestClient, @LocalServerPort private val randomServerPort: Int) : AbstractMongoIntegrationTest() {

    companion object {
        private var accessToken: String? = null
    }

    @BeforeEach
    fun getToken() {
        if (accessToken == null) {
            val userRequestParam = UserRequestParam("tags-creation${Math.random()}@bar.com", "secret")
            val accessTokenResp = WebClient.builder().build()
                    .post()
                    .uri("http://localhost:$randomServerPort/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(userRequestParam), UserRequestParam::class.java)
                    .retrieve()
                    .toEntity(UserResponse::class.java)
                    .block()

            accessTokenResp?.statusCode?.is2xxSuccessful?.let { Assertions.assertTrue(it) }
            accessToken = accessTokenResp?.body?.accessToken
        }
    }

    @Test
    fun test1CreateTags() {
        webTestClient.post().uri("/v1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isCreated
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty
    }

    @Test
    fun test2GetTags() {
        webTestClient.get().uri("/v1/tags")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.children").isMap
                .jsonPath("$.children.portal").isMap
                .jsonPath("$.children.all").isMap
                .jsonPath("$.children.portal").isEmpty
                .jsonPath("$.children.all").isEmpty
    }


}
