package de.oglimmer.linky

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
class LinkController(private var repository: LinkCrudRepository) {

    @GetMapping("/links")
    fun getByTag(@AuthenticationPrincipal jwt: Jwt): Flux<Link> {
        return repository.findByUserid(jwt.subject);
    }

    @PostMapping("/links")
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
                                    "link",
                                    faviconUrl,
                                    jwt.subject))
                        }
            }

    @PutMapping("/links/{linkId}")
    fun update(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable linkId: String,
               @RequestBody linkCreate: LinkCreate): Mono<Link> =
            repository.findByIdAndUserid(linkId, jwt.subject)
                    .map {
                        Link(id = generateId(),
                                linkUrl = completeProtocol(linkCreate.linkUrl),
                                callCounter = it!!.callCounter,
                                lastCalled = it.lastCalled,
                                createdDate = it.createdDate,
                                tags = copyTags(linkCreate.tags),
                                rssUrl = linkCreate.rssUrl,
                                pageTitle = linkCreate.pageTitle!!,
                                notes = linkCreate.notes,
                                type = it.type,
                                faviconUrl = it.faviconUrl,
                                userid = it.userid)
                    }
                    .flatMap {
                        repository.save(it)
                    }

    @GetMapping("/links/{linkId}")
    fun loadOne(@AuthenticationPrincipal jwt: Jwt,
                @PathVariable linkId: String): Mono<Link> =
            repository.findByIdAndUserid(linkId, jwt.subject)

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
