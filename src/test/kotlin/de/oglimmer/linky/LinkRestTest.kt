package de.oglimmer.linky

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.rest.dto.LinkModifyDto
import de.oglimmer.linky.rest.dto.UserRequestParam
import de.oglimmer.linky.rest.dto.UserResponse
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@TestMethodOrder(MethodOrderer.MethodName::class)
class LinkRestTest(@Autowired private val webTestClient: WebTestClient, @LocalServerPort private val randomServerPort: Int) : AbstractMongoIntegrationTest() {

    companion object {
        private var accessToken: String? = null
        private var createdLink: Link? = null
    }

    private val mapper = ObjectMapper().registerModules(KotlinModule(), JavaTimeModule())

    @BeforeEach
    fun getToken() {
        if (accessToken == null) {
            val userRequestParam = UserRequestParam("link-creation${Math.random()}@bar.com", "secret")
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
    fun test1CreateLink() {
        val linkCreate = LinkModifyDto(
                linkUrl = "oglimmer.de",
                tags = listOf("firstTag"),
                rssUrl = null,
                pageTitle = null,
                notes = null
        )
        val responseBody = webTestClient.post().uri("/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .body(Mono.just(linkCreate), linkCreate.javaClass)
                .exchange()
                .expectStatus().isCreated
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .returnResult()
                .responseBody
        createdLink = mapper.readValue(responseBody, Link::class.java)
    }

    @Test
    fun test1bCreateLink() {
        val linkCreate = LinkModifyDto(
                linkUrl = "https://spiegel.de",
                tags = listOf("anothertag"),
                rssUrl = null,
                pageTitle = "this is the title",
                notes = null
        )
        webTestClient.post().uri("/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .body(Mono.just(linkCreate), linkCreate.javaClass)
                .exchange()
                .expectStatus().isCreated
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty
    }

    @Test
    fun test1cCreateLink() {
        val linkCreate = LinkModifyDto(
                linkUrl = "http://geizhals.de",
                tags = listOf("anothertag"),
                rssUrl = null,
                pageTitle = null,
                notes = null
        )
        webTestClient.post().uri("/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .body(Mono.just(linkCreate), linkCreate.javaClass)
                .exchange()
                .expectStatus().isCreated
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty
    }

    @Test
    fun test2GetLink() {
        webTestClient.get().uri("/v1/links/${createdLink!!.id}")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(createdLink!!.id)
                .jsonPath("$.callCounter").isEqualTo(createdLink!!.callCounter)
                .jsonPath("$.createdDate").isNotEmpty
                .jsonPath("$.lastCalled").isNotEmpty
                .jsonPath("$.linkUrl").isEqualTo(createdLink!!.linkUrl)
                .jsonPath("$.pageTitle").isEqualTo(createdLink!!.pageTitle)
                .jsonPath("$.tags").isEqualTo(createdLink!!.tags)
                .jsonPath("$.userid").isEqualTo(createdLink!!.userid)
    }

    @Test
    fun test3GetAllLinks() {
        webTestClient.get().uri("/v1/links")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("\$.length()").isEqualTo(3)
    }

    @Test
    fun test4GetLinksByTags() {
        webTestClient.get().uri("/v1/links/by-tags/firstTag")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("\$.length()").isEqualTo(1)
    }

    @Test
    fun test5UpdateLink() {
        val linkCreate = LinkModifyDto(
                linkUrl = "zimperium.de",
                tags = listOf("firstTag"),
                rssUrl = null,
                pageTitle = null,
                notes = null
        )
        val responseBody = webTestClient.put().uri("/v1/links/${createdLink!!.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .body(Mono.just(linkCreate), linkCreate.javaClass)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(createdLink!!.id)
                .jsonPath("$.linkUrl").isEqualTo("http://zimperium.de")
                .returnResult()
                .responseBody
        createdLink = mapper.readValue(responseBody, Link::class.java)
    }

    @Test
    fun test6Redirect() {
        webTestClient.get().uri("/v1/links/${createdLink!!.id}/redirect")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().is3xxRedirection
                .expectHeader().value("Location", IsEqual.equalTo("http://zimperium.de"))
    }

    @Test
    fun test7DeleteLink() {
        webTestClient.delete().uri("/v1/links/${createdLink!!.id}")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun test8GetLinkAfterDelete() {
        webTestClient.get().uri("/v1/links/${createdLink!!.id}")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .expectStatus().isOk
                .expectBody().isEmpty
    }

}
