package de.oglimmer.linky

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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
    fun create(@AuthenticationPrincipal jwt: Jwt, @RequestBody linkCreate: LinkCreate): Mono<Link> {
        return Favicon.loadFavicon(linkCreate.linkUrl)
                .flatMap { faviconUrl ->
                    repository.save(Link(generateId(),
                            completeProtocol(linkCreate.linkUrl),
                            0,
                            Instant.now(),
                            Instant.now(),
                            copyTags(linkCreate.tags),
                            linkCreate.rssUrl,
                            linkCreate.pageTitle,
                            linkCreate.notes,
                            "link",
                            faviconUrl,
                            jwt.subject))
                }
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
        val pageTitle: String,
        val notes: String?
)
