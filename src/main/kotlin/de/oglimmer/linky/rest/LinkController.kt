package de.oglimmer.linky.rest

import de.oglimmer.linky.dao.LinkCrudRepository
import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.logic.Favicon
import de.oglimmer.linky.logic.TitleProducer
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/links")
class LinkController(private var repository: LinkCrudRepository) {

    @GetMapping
    fun loadAll(@AuthenticationPrincipal jwt: Jwt): Flux<Link> = repository.findByUserid(jwt.subject)


    @GetMapping("/by-tags/{tag}")
    fun getByTag(@AuthenticationPrincipal jwt: Jwt, @PathVariable tag: String): Flux<Link> =
            repository.findByUserIdAndTags(jwt.subject, arrayOf(tag))


    @PostMapping
    fun create(@AuthenticationPrincipal jwt: Jwt, @RequestBody linkCreate: LinkCreate): Mono<Link> =
            TitleProducer.buildTitle(linkCreate).flatMap { title ->
                Favicon.loadFavicon(linkCreate.linkUrl)
                        .flatMap { faviconUrl ->
                            repository.save(Link(generateId(),
                                    completeProtocol(linkCreate.linkUrl),
                                    0,
                                    Instant.now(),
                                    Instant.now(),
                                    copyTags(linkCreate.tags),
                                    linkCreate.rssUrl,
                                    title,
                                    linkCreate.notes,
                                    faviconUrl,
                                    jwt.subject))
                        }
            }

    @PutMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun update(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable linkId: String,
               @RequestBody linkCreate: LinkCreate): Mono<Link> =
            repository.findByIdAndUserid(linkId, jwt.subject)
                    .map {
                        it?.copy(linkUrl = completeProtocol(linkCreate.linkUrl),
                                tags = copyTags(linkCreate.tags),
                                rssUrl = linkCreate.rssUrl,
                                pageTitle = linkCreate.pageTitle ?: it.pageTitle,
                                notes = linkCreate.notes)
                    }
                    .flatMap { repository.save(it!!) }

    @GetMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun loadOne(@AuthenticationPrincipal jwt: Jwt,
                @PathVariable linkId: String): Mono<Link?> = repository.findByIdAndUserid(linkId, jwt.subject)

    @DeleteMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun deleteLink(@PathVariable linkId: String) = repository.deleteById(linkId)

    @GetMapping("/{linkId}/redirect")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun redirect(@AuthenticationPrincipal jwt: Jwt,
                 @PathVariable linkId: String): Mono<ResponseEntity<String>> =
            repository.findByIdAndUserid(linkId, jwt.subject)
                    .map { it?.copy(callCounter = it.callCounter + 1, lastCalled = Instant.now()) }
                    .flatMap { repository.save(it!!) }
                    .map {
                        ResponseEntity
                                .status(HttpStatus.MOVED_PERMANENTLY)
                                .header("Location", it.linkUrl)
                                .body(it.linkUrl)
                    }

    private fun completeProtocol(linkUrl: String): String {
        if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
            return "http://$linkUrl"
        }
        return linkUrl
    }

    private fun copyTags(tags: List<String>): List<String> {
        val returnList = ArrayList(tags)
        if (returnList.isEmpty()) {
            returnList.add("portal")
        }
        if (!returnList.contains("all")) {
            returnList.add("all")
        }
        return returnList
    }

    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }

}

data class LinkCreate(
        val linkUrl: String,
        val tags: List<String>,
        val rssUrl: String?,
        val pageTitle: String?,
        val notes: String?
)
